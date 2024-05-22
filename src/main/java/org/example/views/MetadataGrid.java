package org.example.views;

import com.vaadin.flow.component.grid.Grid;
import org.apache.tika.metadata.Metadata;

public class MetadataGrid extends Grid<String> {
    public MetadataGrid(Metadata metadata) {
        // Metadata keys as rows/items
        setItems(metadata.names());
        addColumn(s -> s).setHeader("Property");
        addColumn(s -> metadata.get(s)).setHeader("Value");
        if (metadata.names().length < 6) {
            // adjust size based on rows if only few rows of data
            setAllRowsVisible(true);
        }
    }
}
