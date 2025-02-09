package exersolver.mcsrfairplay.verification_screen;

import exersolver.mcsrfairplay.MCSRFairplay;
import exersolver.mcsrfairplay.compat.AtumCompat;
import exersolver.mcsrfairplay.mixin.accessor.MinecraftClientAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.ClientResourcePackProfile;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFilesScreen extends Screen {
    private static final String ATUM_REGEX = "^(Random|Set) Speedrun #\\d+( \\(\\d+\\))?$";
    private static final String LOG_REGEX = "^\\d{4}-\\d{2}-\\d{2}-\\d.log.gz$";

    private final Screen parent;

    private Phase phase = Phase.NONE;
    @Nullable
    private Text textOverride;
    @Nullable
    private Path path;

    private ZipFilesExplanationListWidget explanation;

    public ZipFilesScreen(Screen parent) {
        super(new TranslatableText("mcsrfairplay.gui.zip_files.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.phase == Phase.NONE) {
            this.addButton(new ButtonWidget(this.width / 2 - 160, this.height - 27, 150, 20, new TranslatableText("mcsrfairplay.gui.zip_files.skip_zip"), button -> this.onClose()));
            this.addButton(new ButtonWidget(this.width / 2 + 10, this.height - 27, 150, 20, new TranslatableText("mcsrfairplay.gui.zip_files.zip_files"), button -> {
                button.visible = false;
                this.zipFiles();
            }));
        } else {
            this.addButton(new ButtonWidget(this.width / 2 - 160, this.height - 27, 150, 20, new TranslatableText("gui.toTitle"), button -> {
                if (!this.phase.blocking) {
                    this.onClose();
                }
            }));
            this.addButton(new ButtonWidget(this.width / 2 + 10, this.height - 27, 150, 20, new TranslatableText("mcsrfairplay.gui.zip_files.open_directory"), button -> this.openDirectory()));
        }

        String[] explanation = new String[]{
                "For certain categories or when achieving certain times, additional proof may be required to verify your run.",
                "This can include (but may not be limited to) world files and the latest.log of the current minecraft session.",
                "",
                "When using resource packs other than the vanilla resource packs, you are also required to submit their files.",
                "",
                "By pressing 'Zip Files', these files will be zipped and put into the .minecraft/mcsrfairplay/ directory.",
                "Once all your files are zipped, you can press the 'Open Directory' button to view the zipped file.",
                "",
                "Before uploading this file when submitting your run to the leaderboard, please check the submission rules to make sure all required files are included!"
        };

        List<List<StringRenderable>> lines = Arrays.stream(explanation)
                .map(StringRenderable::plain)
                .map(line -> MinecraftClient.getInstance().textRenderer.wrapLines(line, this.width - 50))
                .collect(Collectors.toList());

        this.explanation = this.addChild(new ZipFilesExplanationListWidget(
                this.client, this.width, this.height, 32, this.height - 32, lines,
                3 + lines.stream().mapToInt(line -> line.size() * 10 + 3).sum()
        ));

        super.init();
    }

    private void zipFiles() {
        if (this.phase != Phase.NONE) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        MinecraftServer server = client.getServer();

        String fileName = client.getSession().getUsername() + "-" + (server != null ? server.getSavePath(WorldSavePath.ROOT).getParent().getFileName() : "multiplayer") + "-" + new SimpleDateFormat("yyyy-MM-dd-hh-mm").format(new Date());

        this.phase = Phase.DISCONNECT;
        this.disconnect(client);

        this.phase = Phase.STOP_ATUM;
        this.stopAtum();

        this.phase = Phase.ZIP_FILES;
        this.render(client);
        try {
            this.path = this.zipFiles(client, server, fileName);
            this.phase = Phase.FINISHED;
        } catch (Exception e) {
            MCSRFairplay.LOGGER.error("Failed to zip verification files", e);
            this.phase = Phase.FAILURE;
        }
    }

    private void disconnect(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world != null) {
            world.disconnect();
            client.disconnect(this);
        }
    }

    private void stopAtum() {
        if (MCSRFairplay.HAS_ATUM) {
            AtumCompat.stopRunning();
        }
    }

    private void render(MinecraftClient client) {
        ((MinecraftClientAccessor) client).mcsrfairplay$render(false);
    }

    private Path zipFiles(MinecraftClient client, @Nullable MinecraftServer server, String fileName) throws IOException {
        Files.createDirectories(MCSRFairplay.DIRECTORY);
        Path path = this.getNextUniquePath(fileName);

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            if (server != null) {
                this.zipWorlds(zip, client, server);
            }
            this.zipResourcePacks(zip, client);
            this.zipLogs(zip);
        }

        return path;
    }

    private Path getNextUniquePath(String name) {
        Path path = MCSRFairplay.DIRECTORY.resolve(name + ".zip");
        int i = 1;
        while (Files.exists(path)) {
            path = MCSRFairplay.DIRECTORY.resolve(name + " (" + i++ + ").zip");
        }
        return path;
    }

    private void zipWorlds(ZipOutputStream zip, MinecraftClient client, MinecraftServer server) throws IOException {
        this.zipPath(zip, "world/", server.getSavePath(WorldSavePath.ROOT).getParent());

        List<Path> worlds = this.getWorldsAtum(client, server).orElseGet(() -> this.getWorldsVanilla(client, server));
        for (Path world : worlds) {
            this.zipPath(zip, "additional_worlds/", world);
        }
    }

    private Optional<List<Path>> getWorldsAtum(MinecraftClient client, MinecraftServer server) {
        if (!FabricLoader.getInstance().isModLoaded("atum")) {
            return Optional.empty();
        }
        String world = server.getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString();
        if (!world.matches(ATUM_REGEX)) {
            return Optional.empty();
        }

        Path saves = client.getLevelStorage().getSavesDirectory();
        int countStart = world.indexOf('#') + 1;
        int countEnd = world.indexOf(' ', countStart);
        if (countEnd == -1) {
            countEnd = world.length();
        }
        try {
            String prefix = world.substring(0, countStart);
            int count = Integer.parseInt(world.substring(countStart, countEnd));

            List<Path> worlds = new ArrayList<>();

            // zip 5 previous resets
            for (int i = 1; i <= 5; i++) {
                List<Path> previousWorlds = this.findWorldsAtum(saves, prefix, count - i);
                if (previousWorlds.isEmpty()) {
                    MCSRFairplay.LOGGER.warn("Only found {} old worlds!", i - 1);
                    break;
                }
                worlds.addAll(previousWorlds);
            }

            // zip up to 100 resets after if SeedQueue is loaded
            if (!FabricLoader.getInstance().isModLoaded("seedqueue")) {
                return Optional.of(worlds);
            }

            for (int i = 1; true; i++) {
                List<Path> nextWorlds = this.findWorldsAtum(saves, prefix, count + i);
                if (nextWorlds.isEmpty()) {
                    break;
                }
                worlds.addAll(nextWorlds);
            }

            return Optional.of(worlds);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private List<Path> findWorldsAtum(Path saves, String prefix, int counter) {
        String name = prefix + counter;
        Path world = saves.resolve(name);
        List<Path> worlds = new ArrayList<>();

        // If a Random Speedrun #1234 directory already exists,
        // Atum will instead create Random Speedrun #1234 (1)
        // and we have to zip all of them
        int uniqueNumber = 1;
        while (Files.exists(world)) {
            worlds.add(world);
            world = saves.resolve(name + " (" + uniqueNumber++ + ")");
        }

        return worlds;
    }

    private List<Path> getWorldsVanilla(MinecraftClient client, MinecraftServer server) {
        File world = server.getSavePath(WorldSavePath.ROOT).getParent().toFile();

        SortedSet<Pair<Path, Long>> previousWorlds = new TreeSet<>(Comparator.comparing(Pair::getRight));
        for (File file : Objects.requireNonNull(client.getLevelStorage().getSavesDirectory().toFile().listFiles())) {
            if (file.equals(world)) {
                continue;
            }
            File levelDat = new File(file, "level.dat");
            if (!levelDat.exists()) {
                continue;
            }

            long lastModified = levelDat.lastModified();
            if (previousWorlds.size() < 5) {
                previousWorlds.add(new Pair<>(file.toPath(), lastModified));
                continue;
            }

            Pair<Path, Long> oldestWorld = previousWorlds.first();
            if (lastModified > oldestWorld.getRight()) {
                previousWorlds.remove(oldestWorld);
                previousWorlds.add(new Pair<>(file.toPath(), lastModified));
            }
        }

        if (previousWorlds.size() < 5) {
            MCSRFairplay.LOGGER.warn("Only found {} old worlds!", previousWorlds.size());
        }

        return previousWorlds.stream().map(Pair::getLeft).collect(Collectors.toList());
    }

    private void zipResourcePacks(ZipOutputStream zip, MinecraftClient client) throws IOException {
        for (ClientResourcePackProfile pack : client.getResourcePackManager().getEnabledProfiles()) {
            String name = pack.getName();
            if (name.startsWith("file/")) {
                this.zipPath(zip, "resourcepacks/", client.getResourcePackDir().toPath().resolve(name.substring("file/".length())));
            }
        }
    }

    private void zipLogs(ZipOutputStream zip) throws IOException {
        Path logs = FabricLoader.getInstance().getGameDir().resolve("logs");
        this.zipPath(zip, "logs/", logs.resolve("latest.log"));

        long time = System.currentTimeMillis();
        for (File file : Objects.requireNonNull(logs.toFile().listFiles())) {
            String name = file.getName();
            if (!name.matches(LOG_REGEX)) {
                continue;
            }
            try {
                // zip all logs within two days since the date rounds down
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(name.substring(0, "yyyy-MM-dd".length()));
                if (date.getTime() > time - 172800000) {
                    this.zipPath(zip, "logs/", file.toPath());
                }
            } catch (ParseException ignored) {
            }
        }
    }

    private void zipPath(ZipOutputStream zip, String prefix, Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                zip.putNextEntry(new ZipEntry(prefix + path.getParent().relativize(file)));
                Files.copy(file, zip);
                zip.closeEntry();
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                zip.putNextEntry(new ZipEntry(prefix + path.getParent().relativize(dir) + "/"));
                zip.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void openDirectory() {
        if (this.path != null) {
            Util.getOperatingSystem().open(MCSRFairplay.DIRECTORY.toFile());
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        if (this.phase == Phase.NONE) {
            this.explanation.render(matrices, mouseX, mouseY, delta);
        } else {
            this.drawCenteredText(matrices, this.textRenderer, this.textOverride != null ? this.textOverride : this.phase.text, this.width / 2, 70, 0xFFFFFF);
            if (this.path != null) {
                this.drawCenteredText(matrices, this.textRenderer, StringRenderable.plain(this.path.getFileName().toString()), this.width / 2, 95, 0xFFFFFF);
            }
        }
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.phase != Phase.NONE) {
            MinecraftClient.getInstance().openScreen(new TitleScreen());
            return;
        }
        MinecraftClient.getInstance().openScreen(new ConfirmScreen(confirm -> {
            if (confirm) {
                MinecraftClient.getInstance().openScreen(this.parent);
            } else {
                MinecraftClient.getInstance().openScreen(this);
            }
        }, new TranslatableText("mcsrfairplay.gui.zip_files.confirm_skip"), LiteralText.EMPTY));
    }

    public static boolean captureScreen(Screen screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen current = client.currentScreen;
        if (current == screen || !(current instanceof ZipFilesScreen)) {
            return false;
        }
        ZipFilesScreen zipFilesScreen = (ZipFilesScreen) current;
        if (!zipFilesScreen.phase.blocking) {
            return false;
        }
        zipFilesScreen.textOverride = screen.getTitle();
        zipFilesScreen.render(client);
        zipFilesScreen.textOverride = null;
        return true;
    }

    private enum Phase {
        NONE,
        DISCONNECT("menu.savingLevel", true),
        STOP_ATUM("mcsrfairplay.gui.zip_files.stopping_atum", true),
        ZIP_FILES("mcsrfairplay.gui.zip_files.zipping_files", true),
        FINISHED("mcsrfairplay.gui.zip_files.finished_zipping"),
        FAILURE("mcsrfairplay.gui.zip_files.failed_zipping");

        private final Text text;
        private final boolean blocking;

        Phase() {
            this.text = null;
            this.blocking = false;
        }

        Phase(String key) {
            this(key, false);
        }

        Phase(String key, boolean blocking) {
            this.text = new TranslatableText(key);
            this.blocking = blocking;
        }
    }
}
