import Utils.{decryptData, openFile, verifySignatyre}

import java.nio.file.{Files, Paths}
import java.util._
import javax.mail._
import javax.mail.internet._
import scala.io.Source.fromFile

object Receiver extends App {

  val Array(username, password, _*) = args

  val privateKeyReciever = Utils.loadPrivateKey(openFile("Открыть закрытый ключ"))
  val publicKeyReciever = Utils.loadPublicKey(openFile("Открыть открытый ключ"))

  recieveMessage() //Получение письма

  var source = fromFile("recieved.txt") //Считывание полученного файла
  val recievedMessage = try source.mkString finally source.close()

  source = fromFile("recievedSig.sig") //Считывание полученного файла ЭЦП
  val recievedSig = try source.mkString("") finally source.close()

  val decryptedData = decryptData(recievedMessage, privateKeyReciever)
  Files.write(Paths.get("decrypted.txt"), decryptedData.getBytes) //Запись расшифрованного файла

  verifySignatyre(recievedSig, publicKeyReciever, recievedMessage)

  def recieveMessage() = {
    val properties = new Properties()
    properties.put("mail.debug", "false")
    properties.put("mail.store.protocol", "pop3")
    properties.put("mail.pop3.ssl.enable", "true")
    properties.put("mail.pop3.port", "995")
    val emailSession = Session.getInstance(properties, new Authenticator() {
      override protected def getPasswordAuthentication = new PasswordAuthentication(username, password)
    })
    val emailStore = emailSession.getStore()
    emailStore.connect("pop.mail.ru", username, password)
    val emailFolder = emailStore.getFolder("INBOX");
    emailFolder.open(Folder.READ_ONLY);

    for (i <- emailFolder.getMessageCount - 5 to emailFolder.getMessageCount) {
      val message = emailFolder.getMessage(i)
      println(s"${i} " + "Subject: " + message.getSubject + " From: " + message.getFrom()(0))
      println("---------------------------------")
    }
    println("Введите номер письма")
    val messageNumber = scala.io.StdIn.readInt()

    println(emailFolder.getMessageCount)
    val message = emailFolder.getMessage(messageNumber)
    val multiPart = message.getContent.asInstanceOf[Multipart]
    val numberOfParts = multiPart.getCount
    for (partCount <- 0 until numberOfParts) {
      val part = multiPart.getBodyPart(partCount).asInstanceOf[MimeBodyPart]
      if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition)) { // Получение и сохранение вложений из письма
        if (part.getFileName.contains("sig")) part.saveFile("recievedSig.sig") else
          part.saveFile("recieved.txt")
      }
    }
    println("данные успешно получены")
  }
}
