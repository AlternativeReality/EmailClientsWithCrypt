import java.io.{File, FileInputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64
import javax.crypto.Cipher
import javax.swing.JFileChooser

object Utils {

  def saveKeysToFile(publicKey: PublicKey, privateKey: PrivateKey) = {

    val x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded)
    Files.write(Paths.get("publicKey.key"), x509EncodedKeySpec.getEncoded)

    val pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
      privateKey.getEncoded)
    Files.write(Paths.get("privateKey.key"), pkcs8EncodedKeySpec.getEncoded)
  }

  def loadKeyPair(): KeyPair = {
    import java.io.FileInputStream
    // Read Public Key.
    val filePublicKey = new File("publicKey.key")
    var fis = new FileInputStream("publicKey.key")
    val encodedPublicKey = new Array[Byte](filePublicKey.length.asInstanceOf[Int])
    fis.read(encodedPublicKey)

    // Read Private Key

    val filePrivateKey = new File("privateKey.key")
    fis = new FileInputStream("privateKey.key")
    val encodedPrivateKey = new Array[Byte](filePrivateKey.length.asInstanceOf[Int])
    fis.read(encodedPrivateKey)
    // Generate KeyPair.// Generate KeyPair.

    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKeySpec = new X509EncodedKeySpec(encodedPublicKey)
    val publicKey = keyFactory.generatePublic(publicKeySpec)

    val privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey)
    val privateKey = keyFactory.generatePrivate(privateKeySpec)

    new KeyPair(publicKey, privateKey)
  }

  def loadPublicKey(path: String): PublicKey = {
    val filePublicKey = new File(path)
    var fis = new FileInputStream(path)
    val encodedPublicKey = new Array[Byte](filePublicKey.length.asInstanceOf[Int])
    fis.read(encodedPublicKey)
    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKeySpec = new X509EncodedKeySpec(encodedPublicKey)
    val publicKey = keyFactory.generatePublic(publicKeySpec)
    publicKey
  }

  def loadPrivateKey(path: String): PrivateKey = {
    var fis = new FileInputStream(path)
    val filePrivateKey = new File(path)
    val encodedPrivateKey = new Array[Byte](filePrivateKey.length.asInstanceOf[Int])
    fis.read(encodedPrivateKey)

    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey)
    val privateKey = keyFactory.generatePrivate(privateKeySpec)
    privateKey
  }



  def makeSig(text: String, privateKey: PrivateKey): String = {
    val signature = Signature.getInstance("SHA256withRSA") //Инициализация сигнатуры для ЭЦП
    signature.initSign(privateKey)
    val messageBytes = text.getBytes()
    signature.update(messageBytes)
    val digitalSignature = signature.sign // формироавние сигнатуры ЭЦП
    val byteArrayStr = Base64.getEncoder.encodeToString(digitalSignature)
    byteArrayStr
  }

  def encryptData(secretMessage: String, publicKey: PublicKey): String = {
    //Функция шифрования сообщения
    val encryptCipher = Cipher.getInstance("RSA")
    encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val secretMessageBytes = secretMessage.getBytes(StandardCharsets.UTF_8)
    val encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes)
    val encodedMessage = Base64.getEncoder.encodeToString(encryptedMessageBytes)
    encodedMessage
  }

  def decryptData(data: String, privateKey: PrivateKey): String = {
    //Функция дешифрования сообщения
    val a = Base64.getDecoder.decode(data)
    import javax.crypto.Cipher
    val decryptCipher = Cipher.getInstance("RSA")
    decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
    import java.nio.charset.StandardCharsets
    val decryptedMessageBytes = decryptCipher.doFinal(a)
    val decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8)
    decryptedMessage
  }

  def openFile(title:String): String = {
    val fileChooser = new JFileChooser()
    fileChooser.setCurrentDirectory(new File("./"))
    fileChooser.showDialog(null, title)
    fileChooser.getSelectedFile.getAbsolutePath
  }

  def verifySignatyre(recievedSig:String, publicKeyReciever:PublicKey, recievedMessage: String) ={
    val digitalSignatureBytes = Base64.getDecoder.decode(recievedSig)
    val signature = Signature.getInstance("SHA256withRSA")
    signature.initVerify(publicKeyReciever)
    val messageBytes = recievedMessage.getBytes()
    signature.update(messageBytes)
    val isCorrect = signature.verify(digitalSignatureBytes) //Проверка ЭЦП
    println("Статус проверки ЭЦП:" + isCorrect)
  }
}
