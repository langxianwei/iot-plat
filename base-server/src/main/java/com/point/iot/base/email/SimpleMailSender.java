package com.point.iot.base.email;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

/**
 * 简单邮件（不带附件的邮件）发送器
 */
public class SimpleMailSender {
	Logger logger = Logger.getLogger(SimpleMailSender.class);
	
	/**
	 * 以文本格式发送邮件
	 * 
	 * @param mailInfo
	 *            待发送的邮件的信息
	 */
	public boolean sendTextMail(MailSenderObj mailInfo) {
		// 判断是否需要身份认证
		MyAuthenticator authenticator = null;
		Properties pro = mailInfo.getProperties();
		if (mailInfo.isValidate()) {
			// 如果需要身份认证，则创建一个密码验证器
			authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
		}
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(mailInfo.getFromAddress());
			// 设置邮件消息的发送者
			mailMessage.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
			String[] asToAddr = mailInfo.getToAddress();
			if (asToAddr == null) {
				logger.debug("邮件发送失败，收信列表为空" + mailInfo);
				return false;
			}
			for (int i=0; i<asToAddr.length; i++) {
				if (asToAddr[i] == null || asToAddr[i].equals("")) {
					continue;
				}
				Address to = new InternetAddress(asToAddr[i]);
				mailMessage.addRecipient(Message.RecipientType.TO, to);
			}
			// 设置邮件消息的主题
			mailMessage.setSubject(mailInfo.getSubject());
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());
			// 设置邮件消息的主要内容
			String mailContent = mailInfo.getContent();
			mailMessage.setText(mailContent);
			// 发送邮件
			Transport.send(mailMessage);
			logger.info("发送邮件成功。" + mailInfo);
			return true;
		} catch (MessagingException ex) {
			logger.error("发送邮件失败：" + ex.getMessage() + Arrays.toString(ex.getStackTrace()));
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * 以HTML格式发送邮件
	 * 
	 * @param mailInfo
	 *            待发送的邮件信息
	 */
	public boolean sendHtmlMail(MailSenderObj mailInfo) {
		// 判断是否需要身份认证
		MyAuthenticator authenticator = null;
		Properties pro = mailInfo.getProperties();
		// 如果需要身份认证，则创建一个密码验证器
		if (mailInfo.isValidate()) {
			authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
		}
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
		sendMailSession.setDebug(true);// 设置debug模式 在控制台看到交互信息
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(mailInfo.getFromAddress());
			// 设置邮件消息的发送者
			mailMessage.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
			String[] asToAddr = mailInfo.getToAddress();
			if (asToAddr == null) {
				logger.debug("邮件发送失败，收信列表为空" + mailInfo);
				return false;
			}
			for (int i=0; i<asToAddr.length; i++) {
				if (asToAddr[i] == null || asToAddr[i].equals("")) {
					continue;
				}
				Address to = new InternetAddress(asToAddr[i]);
				// Message.RecipientType.TO属性表示接收者的类型为TO
				mailMessage.addRecipient(Message.RecipientType.TO, to);
			}
			// 设置邮件消息的主题
			mailMessage.setSubject(mailInfo.getSubject());
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());
			// MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
			Multipart mainPart = new MimeMultipart();
			// 创建一个包含HTML内容的MimeBodyPart
			BodyPart html = new MimeBodyPart();
			// 设置HTML内容
			html.setContent(mailInfo.getContent(), "text/html; charset=utf-8");
			mainPart.addBodyPart(html);
			// 将MiniMultipart对象设置为邮件内容
			mailMessage.setContent(mainPart);
			// 发送邮件
			Transport.send(mailMessage);
			logger.info("发送邮件成功。" + mailInfo);
			return true;
		} catch (MessagingException ex) {
			logger.error("发送邮件失败：" + ex.getMessage() + Arrays.toString(ex.getStackTrace()));
		}
		return false;
	}
}
