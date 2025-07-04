package io.github.unjoinable.vanille;

import io.github.unjoinable.vanille.datapack.DatapackLoader;

public class Vanille {

    private Vanille() {
        throw new UnsupportedOperationException("Cannot create instance of Main class");
    }

    public static void main(String[] args) {
        new DatapackLoader();
        while (true) {}
    }
}