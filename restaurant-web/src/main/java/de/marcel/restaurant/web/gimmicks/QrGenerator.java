package de.marcel.restaurant.web.gimmicks;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.omnifaces.cdi.GraphicImageBean;
import org.omnifaces.util.Faces;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Named;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
/*
	Generiert einen QR-Code dynamisch zur Laufzeit, der einen Link auf die gerade aufgerufene Seite enthält.

	Dies funktioniert nur, wenn der User einmal navigiert hat: Der Button zum Anzeigen befindet sich in der NavBar,
	diese lädt den Content des Grafik-Poppups noch bevor der erste Klick ausgeführt wird. In diesem Fall
	kann es noch keine Rückmeldung vom Browser an das Backend geben. Daher wird der Button erst nach Login eingeblendet.

	Die aktuelle URL wird per JavaScript ermittelt und unter dem HttpParameter "paramurl" ans Backend verschickt.
 */

@Named
@ApplicationScoped
public class QrGenerator implements Serializable
{

	private byte[] qrCodeBytes = null;

	public void generateQrCode()
	{
		String qrCodeString = Faces.getRequestParameterMap().get("paramurl");

		Logger.getLogger(this.getClass().getSimpleName()).severe("+# generateQrCode hat erhalten " + qrCodeString );


		Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

		OutputStream out = null;
		try
		{
			BitMatrix matrix = new MultiFormatWriter().encode(qrCodeString, BarcodeFormat.QR_CODE, 350, 350, hintMap);
			BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image,"png", baos);
			qrCodeBytes = baos.toByteArray();

			Logger.getLogger(this.getClass().getSimpleName()).severe("+# QrCode generiert." );

		}
		catch (WriterException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public byte[] getQrCodeBytes()
	{
		//Logger.getLogger(this.getClass().getSimpleName()).severe("+# getQrCodeBytes läuft.  " );
		return qrCodeBytes;
	}

	public void setQrCodeBytes(byte[] qrCodeBytes)
	{
		this.qrCodeBytes = qrCodeBytes;
	}
}
