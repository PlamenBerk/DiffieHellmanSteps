package AlgorithmExample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
//Very simple client that just sends 
//lines to the server and reads lines 
//that the server sends. 
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class FirstClient {

	private static final int privateNum = 15;

	public static void main(String[] args) {

		long myModResult, servModResult, num, mod, secureKey;
		SecretKeySpec secretKey;
		String msgToEncrypt;

		String server = null;
		InetAddress addr = null;

		try {
			addr = InetAddress.getByName(server);
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
		}

		System.out.println("addr = " + addr);

		try (Socket socket = new Socket(addr, FirstServer.PORT);) {
			System.out.println("socket = " + socket);

			BufferedReader sc = new BufferedReader(new InputStreamReader(System.in));

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
					true);

			System.out.print("Send number to server ::  ");
			num = sendNumToServer(sc, in, out);

			System.out.print("Send mod to server ::  ");
			mod = sendNumToServer(sc, in, out);

			myModResult = (int) ((Math.pow(num, privateNum)) % mod);

			servModResult = ioResults(myModResult, in, out);

			secureKey = (long) (Math.pow(servModResult, privateNum)) % mod;
			System.out.println("Secure key ::  " + secureKey);

			secretKey = generateSecureKey(secureKey);

			System.out.print("Send msg ::  ");
			msgToEncrypt = sc.readLine();

			String encodedString = encodeMessage(secretKey, msgToEncrypt);

			System.out.println("Message ::  " + msgToEncrypt);
			System.out.println("Encrypted message ::  " + encodedString);
			out.println(encodedString);

		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println("closing...");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			System.err.println(e.getMessage());
		} catch (IllegalBlockSizeException e) {
			System.err.println(e.getMessage());
		} catch (BadPaddingException e) {
			System.err.println(e.getMessage());
		}

	}

	private static String encodeMessage(SecretKeySpec secretKey, String msgToEncrypt)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		String encodedString = Base64.getEncoder().encodeToString(cipher.doFinal(msgToEncrypt.getBytes("UTF-8")));
		return encodedString;
	}

	private static SecretKeySpec generateSecureKey(long secureKey)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		SecretKeySpec secretKey;
		String secure = Long.toString(secureKey);
		MessageDigest sha = null;
		byte[] key = secure.getBytes("UTF-8");
		sha = MessageDigest.getInstance("SHA-1");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16);
		secretKey = new SecretKeySpec(key, "AES");
		return secretKey;
	}

	private static int ioResults(long myModResult, BufferedReader in, PrintWriter out) throws IOException {
		System.out.print("Sended mod result ::  " + myModResult);
		System.out.println();
		out.println(myModResult);

		String s = in.readLine();
		int recieveFromServerInt = Integer.parseInt(s);
		System.out.println("Server mod result ::  " + recieveFromServerInt);
		int servModResult = recieveFromServerInt;
		return servModResult;
	}

	private static int sendNumToServer(BufferedReader sc, BufferedReader in, PrintWriter out) throws IOException {

		int sendToServ = Integer.parseInt(sc.readLine());
		out.println(sendToServ);
		int num = sendToServ;

		String recieveFromServer = in.readLine();
		System.out.println("Server response ::  " + recieveFromServer);
		return num;
	}

	public static String getSalt() throws Exception {
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[20];
		sr.nextBytes(salt);
		return new String(salt);
	}
}