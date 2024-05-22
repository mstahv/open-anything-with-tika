package org.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.Route;
import org.apache.tika.exception.WriteLimitReachedException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.vaadin.firitin.components.RichText;

import java.io.File;
import java.io.InputStream;

@Route
public class MainView extends VerticalLayout {
    static final int MB = 1024 * 1024;

    VerticalLayout result = new VerticalLayout();
    Upload upload = new Upload();

    public MainView() {
        add(new RichText().withMarkDown("""
                # WTF - What the File!?
                
                This app let's you update files, parses their content with Apache Tika 
                and displays the extracted metadata & text in your the browsers. Tika 
                can parse a number of different file formats like docs, spreadsheets, 
                images, PDFs, including compressed files. 1M as max file size (Spring Boot default).
                """));
        add(upload, result);

        FileBuffer r = new FileBuffer();
        upload.setReceiver(r);
        upload.addSucceededListener(e -> {
            File tmpFile = r.getFileData().getFile();
            String fileNameFromBrowser = e.getFileName();
            previewContent(fileNameFromBrowser, tmpFile);
            tmpFile.delete();
        });

    }

    private void previewContent(String originalFileName, File tmpFile) {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFileName);
        metadata.set("File size", tmpFile.length() + "B");
        try (InputStream stream = TikaInputStream.get(tmpFile)) {
            parser.parse(stream, handler, metadata);
            displayParsingResults(metadata, handler);
        } catch (WriteLimitReachedException ex) {
            Notification.show(ex.getMessage());
            displayParsingResults(metadata, handler);
        } catch (Exception ex) {
            result.add(new H2("Parsing Data failed: " + ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }

    private void displayParsingResults( Metadata metadata, BodyContentHandler handler) {
        result.removeAll();
        result.add(new H2("Metadata:"));
        result.add(new MetadataGrid(metadata));
        result.add(new H2("Extracted text:"));
        var extractedText = new Pre(handler.toString());
        extractedText.getStyle().setPadding("1em");
        result.add(extractedText);
    }

}
