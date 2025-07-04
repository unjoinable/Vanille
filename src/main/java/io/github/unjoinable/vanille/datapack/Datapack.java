package io.github.unjoinable.vanille.datapack;

import java.util.List;
import java.util.zip.ZipEntry;

public interface Datapack<T> {

    void load(List<ZipEntry> entries);

}
