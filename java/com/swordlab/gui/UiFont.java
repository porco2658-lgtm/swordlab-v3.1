package com.swordlab.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/** Outfit font (OFL) via assets/swordlab/font/ui*.json. If a Minecraft update changes Style.withFont, fix it here only. */
final class UiFont {
    private static final Style REGULAR = Style.EMPTY.withFont(
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("swordlab", "ui")));
    private static final Style BOLD = Style.EMPTY.withFont(
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("swordlab", "ui_bold")));

    private UiFont() {}

    static Component c(String s) { return Component.literal(s).withStyle(REGULAR); }
    static Component b(String s) { return Component.literal(s).withStyle(BOLD); }
    static Component bi(String s) { return Component.literal(s).withStyle(BOLD.withItalic(true)); }
}
