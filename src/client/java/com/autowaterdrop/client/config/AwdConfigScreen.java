package com.autowaterdrop.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class AwdConfigScreen extends Screen {
    private final Screen parent;

    public AwdConfigScreen(Screen parent) {
        super(Text.literal("Auto WaterDrop Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = this.height / 6;
        int x = this.width / 2 - 100;

        // Min fall height slider
        this.addDrawableChild(new SimpleIntSlider(x, y, 200, 20, Text.literal("Min height: "), AwdConfig.get().minFallHeightBlocks, 3, 20, value -> {
            AwdConfig.get().minFallHeightBlocks = value;
            AwdConfig.save();
        }));
        y += 24;

        // Activation lead slider
        this.addDrawableChild(new SimpleIntSlider(x, y, 200, 20, Text.literal("Lead ticks: "), AwdConfig.get().activationLeadTicks, 1, 6, value -> {
            AwdConfig.get().activationLeadTicks = value;
            AwdConfig.save();
        }));
        y += 24;

        // Auto pickup slider (0=off)
        this.addDrawableChild(new SimpleIntSlider(x, y, 200, 20, Text.literal("Auto pickup ticks: "), AwdConfig.get().autoPickupTicks, 0, 6, value -> {
            AwdConfig.get().autoPickupTicks = value;
            AwdConfig.save();
        }));
        y += 24;

        // Toggles
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Restore slot: " + AwdConfig.get().restorePreviousSlot), btn -> {
            AwdConfig.get().restorePreviousSlot = !AwdConfig.get().restorePreviousSlot;
            btn.setMessage(Text.literal("Restore slot: " + AwdConfig.get().restorePreviousSlot));
            AwdConfig.save();
        }).dimensions(x, y, 200, 20).build());
        y += 24;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Neighbor search: " + AwdConfig.get().searchNeighborPlacements), btn -> {
            AwdConfig.get().searchNeighborPlacements = !AwdConfig.get().searchNeighborPlacements;
            btn.setMessage(Text.literal("Neighbor search: " + AwdConfig.get().searchNeighborPlacements));
            AwdConfig.save();
        }).dimensions(x, y, 200, 20).build());
        y += 24;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("HUD: " + AwdConfig.get().showHud), btn -> {
            AwdConfig.get().showHud = !AwdConfig.get().showHud;
            btn.setMessage(Text.literal("HUD: " + AwdConfig.get().showHud));
            AwdConfig.save();
        }).dimensions(x, y, 200, 20).build());
        y += 24;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Safe mode extra tick: " + AwdConfig.get().safeModeExtraTick), btn -> {
            AwdConfig.get().safeModeExtraTick = !AwdConfig.get().safeModeExtraTick;
            btn.setMessage(Text.literal("Safe mode extra tick: " + AwdConfig.get().safeModeExtraTick));
            AwdConfig.save();
        }).dimensions(x, y, 200, 20).build());
        y += 24;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), btn -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(x, y, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private static final class SimpleIntSlider extends SliderWidget {
        private final int min;
        private final int max;
        private final java.util.function.IntConsumer consumer;
        private final String labelPrefix;

        SimpleIntSlider(int x, int y, int w, int h, Text label, int current, int min, int max, java.util.function.IntConsumer consumer) {
            super(x, y, w, h, Text.literal("") , 0.0);
            this.min = min;
            this.max = max;
            this.consumer = consumer;
            this.labelPrefix = label.getString();
            this.value = (current - min) / (double)(max - min);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int v = getInt();
            setMessage(Text.literal(labelPrefix + v));
        }

        @Override
        protected void applyValue() {
            consumer.accept(getInt());
        }

        private int getInt() {
            return (int)Math.round(min + value * (max - min));
        }
    }
}

