package me.kartikarora.transfersh.custom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import retrofit.mime.TypedFile;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.custom
 * Project : Transfer.sh
 * Date : 7/19/17
 */

public class CountingTypedFile extends TypedFile {

    private static final int BUFFER_SIZE = 4096;

    private final FileUploadListener listener;

    public CountingTypedFile(String mimeType, File file, FileUploadListener listener) {
        super(mimeType, file);
        this.listener = listener;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {

        byte[] buffer = new byte[BUFFER_SIZE];
        long total = 0;

        try (FileInputStream in = new FileInputStream(super.file())) {

            int read;
            while ((read = in.read(buffer)) != -1) {

                total += read;
                out.write(buffer, 0, read);
                if (this.listener != null)
                    this.listener.uploaded(total);

            }
        }
    }

    public interface FileUploadListener {
        void uploaded(long num);
    }
}
