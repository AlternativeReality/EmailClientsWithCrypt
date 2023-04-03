import Utils.{makeSig, openFile}

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.Properties
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}

object Sender extends App {

  val Array(username, password, _*) = args

  val messageFileName = "message.txt"
  val sigFileName = "sig.sig"

  val privateKeySender = Utils.loadPrivateKey(openFile("Открыть закрытый ключ"))
  val publicKeySender = Utils.loadPublicKey(openFile("Открыть открытый ключ"))

  println("Введите текстовое письмо")
  val message = scala.io.StdIn.readLine()
  println(message)

  val encrytedData = Utils.encryptData(message, publicKeySender)
  val signature = makeSig(encrytedData, privateKeySender)

  Files.write(Paths.get("message.txt"), encrytedData.getBytes) //Запись зашифрованного файла
  Files.write(Paths.get("sig.sig"), signature.getBytes) //Запись цифровой подписи

  sendEmailWithEncryptedFile()

  def sendEmailWithEncryptedFile() = {
    val to = "morgan900@mail.ru"
    val properties = new Properties() //Конфигурация для работы с email
    properties.put("mail.smtp.host", "smtp.mail.ru")
    properties.put("mail.smtp.starttls.enable", "true")
    properties.put("mail.smtp.ssl.trust", "smtp.mail.ru")
    properties.put("mail.smtp.auth", "true")

    val session = Session.getDefaultInstance(properties, new Authenticator() {
      override protected def getPasswordAuthentication = new PasswordAuthentication(username, password)
    })

    val message = new MimeMessage(session);
    message.setFrom(new InternetAddress(username));
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))
    message.setSubject("digital signature")

    val messageBodyPart = new MimeBodyPart();
    messageBodyPart.setText("digital signature");

    val attachmentPart = new MimeBodyPart
    val attachmentPart2 = new MimeBodyPart
    attachmentPart.attachFile(new File(messageFileName))
    attachmentPart2.attachFile(new File(sigFileName))
    val multipart = new MimeMultipart
    multipart.addBodyPart(attachmentPart)
    multipart.addBodyPart(attachmentPart2)
    multipart.addBodyPart(messageBodyPart)
    message.setContent(multipart)
    Transport.send(message) // Отправка письма с зашифрованным файлом и ЭЦП
  }


}