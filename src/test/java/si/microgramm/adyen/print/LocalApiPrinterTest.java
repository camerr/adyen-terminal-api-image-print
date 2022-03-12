package si.microgramm.adyen.print;

import com.adyen.Config;
import com.adyen.enums.Environment;
import com.adyen.model.nexo.DocumentQualifierType;
import com.adyen.model.nexo.OutputContent;
import com.adyen.model.nexo.OutputFormatType;
import com.adyen.model.nexo.OutputText;
import com.adyen.model.nexo.PrintOutput;
import com.adyen.model.nexo.ResponseModeType;
import com.adyen.model.terminal.security.SecurityKey;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import si.microgramm.adyen.AdyenLocalApiImpl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class LocalApiPrinterTest {

    public static final String ENDPOINT = "https://192.168.158.30";
    public static final String TERMINAL_ID = "S1F2-000158213600432";
    public static final String CASH_REGISTER_ID = "CR1";
    public static final String SECURITY_KEY_IDENTIFIER = "CryptoKeyIdentifier12345";
    public static final String SECURITY_KEY_PASSPHRASE = "p@ssw0rd123456";
    public static final int SECURITY_KEY_VERSION = 1;
    public static final int SECURITY_KEY_CRYPTO_VERSION = 1;

    private static AdyenLocalApiImpl localApi;

    private static final String CERT =
            "-----BEGIN CERTIFICATE-----\n" +
                    "MIIGMDCCBBigAwIBAgIJAO5R1c+xM70ZMA0GCSqGSIb3DQEBCwUAMIGkMQswCQYD\n" +
                    "VQQGEwJOTDELMAkGA1UECAwCTkgxEjAQBgNVBAcMCUFtc3RlcmRhbTETMBEGA1UE\n" +
                    "CgwKQWR5ZW4gQi5WLjERMA8GA1UECwwIQWR5ZW4gQ0ExJzAlBgNVBAMMHkFkeWVu\n" +
                    "IFRlc3QgVGVybWluYWwgRmxlZXQgUm9vdDEjMCEGCSqGSIb3DQEJARYUcG9zc3Vw\n" +
                    "cG9ydEBhZHllbi5jb20wHhcNMTgwNDIwMTEwOTA4WhcNNDgwNDEyMTEwOTA4WjCB\n" +
                    "pDELMAkGA1UEBhMCTkwxCzAJBgNVBAgMAk5IMRIwEAYDVQQHDAlBbXN0ZXJkYW0x\n" +
                    "EzARBgNVBAoMCkFkeWVuIEIuVi4xETAPBgNVBAsMCEFkeWVuIENBMScwJQYDVQQD\n" +
                    "DB5BZHllbiBUZXN0IFRlcm1pbmFsIEZsZWV0IFJvb3QxIzAhBgkqhkiG9w0BCQEW\n" +
                    "FHBvc3N1cHBvcnRAYWR5ZW4uY29tMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIIC\n" +
                    "CgKCAgEAzTmJiMsCwgzEAaG3mdry7UCn0p6sPL9dfuPv+qGTfV+VL9iwmryGjk7n\n" +
                    "00caJJFf+OTT8wODDvEE8NejqzY9iczmLPkfcTau2WZd7M5oL+FPy+766M5z2Qy0\n" +
                    "rgnxcgRoS/qrebVTlEO29sSIG4zOrVX+lqJnoAO+TR4gxFK+W6R9kTm45VOodFHy\n" +
                    "sDDY47DN2ZQzIJKbrZ5OE6nm2WS5wcQcw4g1019D3n67EZ7QIwnoK69eWWYo6hOT\n" +
                    "E4L9O0reyEHPUJ1uUBD/qNmctjyitNefixdBsNfScY6+7QHHTp/kJXabrY5pVuZl\n" +
                    "2sDkWjeNZ9Wgo0Md8zfgxbMT+W+KrRuaar48rO18hJf6joKdIUu8DhxNZJCskpco\n" +
                    "kNZmA7dLrm81mICA7EQ/ifJdLMEXcTCmrwUrFQZPnVdUpjAakzLNWzQdsmxNBdJp\n" +
                    "uTgSqDgMTodG58r2eT4OoqlKwtqbSiEWjpQNnZ7g3JYHjaY1kTGIgX2sMD6HPWS4\n" +
                    "Kv3TYFB1bsOX1CeQZfsIkNKYlhix3JkzjjOOJK8Vf1S7T0iYMDKPjgrAWy3aDCLC\n" +
                    "lHFyejrw2eZDzHCjNZ6kCgEBtqlG4Xxry99Qt8FQWFE4Fp4XPIzKmz9CAm0IQKLH\n" +
                    "o2+RbyZO1fd+8SLqA/R4owZ0iTELmlu8XFQ3nhQf5PemOOsktdkCAwEAAaNjMGEw\n" +
                    "HQYDVR0OBBYEFKaVqmrptZprIk2h8HKF6Dfi3tsMMB8GA1UdIwQYMBaAFKaVqmrp\n" +
                    "tZprIk2h8HKF6Dfi3tsMMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/BAQDAgGG\n" +
                    "MA0GCSqGSIb3DQEBCwUAA4ICAQBMuaJQvsz5vKXP/mee4Eu0QXKglLIzVD3aTyeB\n" +
                    "JDpxajk0Nbl4GhXCn/2LvkbFbeNUiNmBm940FYmr7m0MrKVZXheG90RrJLVVABec\n" +
                    "/nc55XeNT+2X1/RZkJfSeldFuZ1Foew+VHCmIcsrCscxnl07Yts1wriRrEu5tAby\n" +
                    "7WPMjw/moro+eOlefsyjGqWq2r6OQY8h5IB54sI6i4fUMsXIkPC78lCXDLpGSl49\n" +
                    "RL/2I7h3uUPBwiyyB43+HJhytNmp9m/6gyNuGwIIOWlHJsTlch1OkpjyPGMF7l2w\n" +
                    "qNSPwD4p8unjBnakAHmkJc4ywc7n7MpAu2lDvo3W1OSu0dzfZcjQQPhrdncC4ys9\n" +
                    "EzFvUN6UwiKDefBQhnRX/3skL3iSsrENirFLFy+Fk7Qs5ZUpvDZQXtRaFs3aNB1e\n" +
                    "2bDCfB+Xr64BoeQNmmzZ5eW4NAgPeeaVHt5JU837E+W3X13YwJOrdJ0fc2ZSAiCD\n" +
                    "ci7Vb7CAUb5jyVHv6zmKy7kP6jAAWZWv6ujxeik6Qukcp1tLnZoF9QZ2JMcsDFLh\n" +
                    "30wvyz3aCEsdgHguGu5OKLj7TMCqCfBWxxkwBs8DWD6SKAgsHqwie257S3XcMc4n\n" +
                    "0JFAn129/n6Lr8Jek72qDOp/zAt1eXtut75wZroRr9jr7bw6tkNQliJ+kvjEJGhJ\n" +
                    "Ujs0rA==\n" +
                    "-----END CERTIFICATE-----";

    @BeforeClass
    public static void setup() {
        Config config = new Config();

        config.setTerminalApiLocalEndpoint(ENDPOINT);
        config.setEnvironment(Environment.TEST);
        config.setTerminalCertificate(getTerminalCertificate());

        SecurityKey securityKey = new SecurityKey();
        securityKey.setKeyVersion(SECURITY_KEY_VERSION);
        securityKey.setAdyenCryptoVersion(SECURITY_KEY_CRYPTO_VERSION);
        securityKey.setKeyIdentifier(SECURITY_KEY_IDENTIFIER);
        securityKey.setPassphrase(SECURITY_KEY_PASSPHRASE);

        localApi = new AdyenLocalApiImpl(config, securityKey, CASH_REGISTER_ID, TERMINAL_ID);
    }

    @Test
    public void testPrintMixedContent() {

        try {

            List<PrintOutput> outputs = new ArrayList<>();

            outputs.add(createTextPrintOutput("Printing image from file..."));

            outputs.add(createImagePrintOutput(toXml(encodeFileToBase64String(new File("qr_code.png")))));

            outputs.add(createTextPrintOutput("Printing QR code as image..."));

            outputs.add(createImagePrintOutput(toXml(generateQrCodeBase64String("http://www.google.com"))));

            outputs.add(createTextPrintOutput("End of print."));

            localApi.printOnTerminalPrinter(outputs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PrintOutput createImagePrintOutput(String xhtml) {
        PrintOutput printOutput = createPrintOutput(OutputFormatType.XHTML);
        printOutput.getOutputContent().setOutputXHTML(Base64.getEncoder().encode(xhtml.getBytes(StandardCharsets.UTF_8)));
        return printOutput;
    }

    private PrintOutput createTextPrintOutput(String value) {
        PrintOutput printOutput = createPrintOutput(OutputFormatType.TEXT);
        printOutput.getOutputContent().getOutputText().add(createOutputText(value));
        return printOutput;
    }

    private OutputText createOutputText(String value) {
        OutputText outputText = new OutputText();
        outputText.setText(value);
        outputText.setEndOfLineFlag(true);
        return outputText;
    }

    private PrintOutput createPrintOutput(OutputFormatType formatType) {
        PrintOutput output = new PrintOutput();
        output.setDocumentQualifier(DocumentQualifierType.DOCUMENT);
        output.setResponseMode(ResponseModeType.PRINT_END);

        OutputContent content = new OutputContent();
        content.setOutputFormat(formatType);
        output.setOutputContent(content);
        return output;
    }

    public static BufferedImage generateQrCodeImage(String value) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(value, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private static Certificate getTerminalCertificate() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificate(new ByteArrayInputStream(CERT.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeFileToBase64String(File file) throws IOException {
        return Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file));
    }

    private String generateQrCodeBase64String(String value) throws Exception {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final BufferedImage image = generateQrCodeImage(value);

        try {
            ImageIO.write(image, "png", os);

            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    private String toXml(String base64Image) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<img src=\"data:image/png;base64, " + base64Image + "\"/>";
    }

}
