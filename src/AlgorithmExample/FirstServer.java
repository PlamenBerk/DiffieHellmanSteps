package AlgorithmExample;

// FirstServer.java 
// Very simple server that just 
// echoes whatever the client sends. 
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class FirstServer {

	private static final int privateNum = 3;
	public static final int PORT = 8080;

	public static void main(String[] args) {
		int num, mod, secureKey, clientModResult;
		SecretKeySpec secretKey;
		String recieveFromClientStr;

		try (ServerSocket s = new ServerSocket(PORT);) {
			System.out.println("Started: " + s);

			Socket socket = s.accept();
			System.out.println("Connection accepted: " + socket);

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
					true);

			num = receiveNumFromClient(in, out);
			mod = receiveNumFromClient(in, out);
			clientModResult = receiveNumFromClient(in);

			sendModResult(num, mod, out);

			secureKey = (int) (Math.pow(clientModResult, privateNum) % mod);
			System.out.println("Secure key ::  " + secureKey);

			recieveFromClientStr = in.readLine();

			secretKey = generateSecureKey(secureKey);

			String decryptedStr;

			decryptedStr = decryptMessage(secretKey, recieveFromClientStr);

			System.out.println("Encrypted message ::  " + recieveFromClientStr);
			System.out.println("Client (decrypted message) ::  " + decryptedStr);

		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
		} catch (InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private static String decryptMessage(SecretKeySpec secretKey, String recieveFromClientStr)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		String decryptedStr = new String(cipher.doFinal(Base64.getDecoder().decode(recieveFromClientStr)));
		return decryptedStr;
	}

	private static SecretKeySpec generateSecureKey(int secureKey)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] key;
		SecretKeySpec secretKey;
		String secure = Integer.toString(secureKey);
		MessageDigest sha = null;
		key = secure.getBytes("UTF-8");
		sha = MessageDigest.getInstance("SHA-1");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16);
		secretKey = new SecretKeySpec(key, "AES");
		return secretKey;
	}

	private static void sendModResult(int num, int mod, PrintWriter out) {
		int myModResult = (int) ((Math.pow(num, privateNum)) % mod);
		System.out.println("Sended result ::  " + myModResult);
		out.println(myModResult);
	}

	private static int receiveNumFromClient(BufferedReader in) throws IOException {
		int recieveFromClient = Integer.parseInt(in.readLine());
		System.out.println("Client ::  " + recieveFromClient);
		int num = recieveFromClient;
		return num;
	}

	private static int receiveNumFromClient(BufferedReader in, PrintWriter out) throws IOException {
		out.println("Accept");
		return receiveNumFromClient(in);
	}
}