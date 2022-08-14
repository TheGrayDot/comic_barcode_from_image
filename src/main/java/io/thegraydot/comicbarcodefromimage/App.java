package io.thegraydot.comicbarcodefromimage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

public class App {
    public static void main(String[] args) {
        // Check file is provided
        if (args.length < 1 || args[0].isBlank()) {
            System.out.println("[+] Error: Please supply file.");
            return;
        }

        // Get provided file
        String inputFileName = args[0];
        System.out.println("[+] Processing: " + inputFileName);

        // Check provided file
        File inputFile = new File(inputFileName);
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.out.println("[+] Error: Please supply a valid file.");
            return;
        }

        // Configure zxing hints
        // Set barcode formats to only known ones used in comics
        List<BarcodeFormat> possibleFormats = Arrays.asList(
                BarcodeFormat.UPC_A,
                BarcodeFormat.EAN_13,
                BarcodeFormat.UPC_EAN_EXTENSION);
        // Set possible barcode extensions to length 2 or 5 digits
        int[] possibleExtensions = { 2, 5 };
        // Populate hints variable to pass to scanner
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, possibleFormats);
        hints.put(DecodeHintType.ALLOWED_EAN_EXTENSIONS, possibleExtensions);

        // Implement try harder flag
        // Research then potetinal implement (toggleable)
        // hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        // Read in image
        BufferedImage image;
        try {
            image = ImageIO.read(inputFile);
        } catch (IOException e) {
            System.out.println("[+] Error: Please supply a valid file.");
            return;
        }
        
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        MultiFormatReader multiFormatReader = new MultiFormatReader();
        MultipleBarcodeReader reader = new GenericMultipleBarcodeReader(multiFormatReader);

        Result[] results = null;

        // Below will throw not found error if no barcode found

        try {
            results = reader.decodeMultiple(bitmap, hints);
        } catch (NotFoundException e) {
            // Pass, it means there is no barcode found
            System.out.println("[+] Error: No barcode found.");
        }

        for (Result result : results) {
            // Get the base barcode (no extension)
            String barcode = result.getText();

            // Get the extension (EAN-2/EAN-5)
            Map<ResultMetadataType, Object> resultMetadata = result.getResultMetadata();
            Object extension = resultMetadata.get(ResultMetadataType.UPC_EAN_EXTENSION);
            String fullBarcode = barcode + extension;

            System.out.println("[+] " + fullBarcode);
        }
    }
}
