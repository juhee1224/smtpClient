import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;


public class EmailSenderGUI extends JFrame {
    private JTextField recipientField;
    private JTextField ccField;
    private JTextField subjectField;
    private JTextArea contentField;
    private JTextField delayField;
    private JButton sendButton;
    private JButton openButton;
    private JTextField filePathField;
    private static final String SMTP_SERVER = "smtp.gmail.com";
    private static final int SMTP_PORT = 465; // SSL port for Gmail
    private static final String SENDER_EMAIL = "txnbyf24@gmail.com";
    private static final String PASSWORD = "xqem hcyr ruzu fndc";

    public EmailSenderGUI() {
        setTitle("Email Sender");
          setSize(550, 400);
          setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          setLayout(new GridBagLayout());
          GridBagConstraints gbc = new GridBagConstraints();
          gbc.insets = new Insets(5, 5,10,10);

          // Recipient
          gbc.gridx = 0;
          gbc.gridy = 0;
          add(new JLabel("Recipient:"), gbc);
          recipientField = new JTextField(20);
          
          add(recipientField, new GridBagConstraints().gridx = 1);

          // CC
          gbc.gridx = 0;
          gbc.gridy = 1;
          add(new JLabel("CC:"), gbc);
          ccField = new JTextField(20);
          gbc.gridx = 1;
          add(ccField, gbc);

          // Subject
          gbc.gridx = 0;
          gbc.gridy = 2;
          add(new JLabel("Subject:"), gbc);
          subjectField = new JTextField(20);
          gbc.gridx = 1;
          add(subjectField, gbc);

          // Content
          gbc.gridx = 0;
          gbc.gridy = 3;
          add(new JLabel("Content:"), gbc);
          contentField = new JTextArea(5, 20);
          contentField.setLineWrap(true);
          JScrollPane contentScrollPane = new JScrollPane(contentField);
          gbc.gridx = 1;
          add(contentScrollPane, gbc);

          // Open File
          openButton = new JButton("Open file"); // Add your file icon path
          openButton.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                  JFileChooser fileChooser = new JFileChooser();
                  int returnValue = fileChooser.showOpenDialog(null);
                  if (returnValue == JFileChooser.APPROVE_OPTION) {
                      File selectedFile = fileChooser.getSelectedFile();
                      filePathField.setText(selectedFile.getAbsolutePath());
                  }
              }
          });
          gbc.gridx = 0;
          gbc.gridy = 4;
          add(openButton, gbc);
          filePathField = new JTextField(20);
          gbc.gridx = 1;
          add(filePathField, gbc);

          // Delay
          gbc.gridx = 0;
          gbc.gridy = 5;
          add(new JLabel("Delay(yyyy-MM-dd HH:mm):"), gbc);
          delayField = new JTextField(20);
          gbc.gridx = 1;
          add(delayField, gbc);

          // Send Button
          sendButton = new JButton("Send");
          sendButton.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                  EmailSenderGUI.sendEmail(
                          recipientField.getText(),
                          ccField.getText(),
                          subjectField.getText(),
                          contentField.getText(),
                          filePathField.getText(),
                          delayField.getText()
                  );
              }
          });
          gbc.gridx = 0;
          gbc.gridy = 6;
          gbc.gridwidth = 2;
          add(sendButton, gbc);

          setVisible(true);
    }

    private static boolean isValidEmailAddress(String email) { // 입력한 것이 메일주소가 맞는지 확인
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$"; // 공백이 없는 대소문자+숫자 @ 점(.)을 포함한 하나 이상의 문자가 연속으로 나올 수 있음을 나타냅니다
        return email.matches(regex); // email이 regex형식이 맞는지 확인
    }

    public static void sendEmail(String recipientEmail, String ccEmail, String subject, String content, String attachmentPath, String dateInput) {
        Thread thread = new Thread(() -> {
            if (!isValidEmailAddress(recipientEmail)) {
                System.out.println("유효하지 않은 이메일 주소입니다.");
                JOptionPane.showMessageDialog(null, "유효하지 않은 이메일 주소입니다. 다시 입력해주세요.", "Warning", JOptionPane.WARNING_MESSAGE);
                // 경고창 -> 유효하지 않은 이메일 주소입니다.
                return;  // 메인 메서드 종료 및 이메일 전송 중지
            }
            if (!isValidEmailAddress(ccEmail)&&!ccEmail.isEmpty()) {
                System.out.println("유효하지 않은 참조메일 주소입니다.");
                JOptionPane.showMessageDialog(null, "유효하지 않은 참조메일 주소입니다. 다시 입력해주세요.", "Warning", JOptionPane.WARNING_MESSAGE);
                // 경고창 -> 유효하지 않은 이메일 주소입니다.
                return;  // 메인 메서드 종료 및 이메일 전송 중지
            }
            
            long delayTime;
            if(!dateInput.isEmpty()){ // 예악일을 입력하면 예약일까지 대기 후 전송
                LocalDateTime currentDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime targetDateTime;
                try {
                    // 입력 받은 날짜가 형식에 맞는지 검사
                    targetDateTime = LocalDateTime.parse(dateInput, formatter);
                } catch (DateTimeParseException e) {
                    // 형식에 맞지 않는 경우 경고창 띄우기
                    JOptionPane.showMessageDialog(null, "입력한 날짜의 형식이 잘못되었습니다.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                delayTime = ChronoUnit.SECONDS.between(currentDate, targetDateTime);

                if (delayTime < 0) {
                    System.out.println("입력한 날짜는 이미 지났습니다.");
                    int response = JOptionPane.showConfirmDialog(null, "입력한 날짜는 이미 지났습니다. 지금 전송하시겠습니까?", "Warning", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        System.out.println("You selected: Yes");
                        delayTime = 0;
                    } else if (response == JOptionPane.NO_OPTION) {
                        System.out.println("You selected: No");
                        return;
                    } else {
                        System.out.println("You closed the dialog without selection");
                        return;
                    }
                    // 여기서 경고창 -> 입력한 날짜는 이미 지났습니다.
                } 
            }
            else{
                delayTime = 0;
        }
            try {
                if(!dateInput.isEmpty()){
                    System.out.println(delayTime + " 초 뒤에 메일을 보냅니다.");
                    if(delayTime != 0){
                        JOptionPane.showMessageDialog(null,"에약 완료 "+ dateInput + " 에 메일을 보냅니다.", "에약 완료", JOptionPane.INFORMATION_MESSAGE);
                        Thread.sleep(delayTime * 1000);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault(); // 기본 SSL 소켓 팩토리를 반환합니다. 소켓 팩토리는 ssl소켓 생성하는데 사용
                try (SSLSocket socket = (SSLSocket) factory.createSocket(SMTP_SERVER, SMTP_PORT);
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    String fileName = new File(attachmentPath).getName();

                     /*
                    sendMessage(writer, "HELO google.com");
                    receiveMessage(reader);
                    sendMessage(writer, "AUTH LOGIN");
                    receiveMessage(reader);
                    */

                    sendMessage(writer, "HELO google.com");
                    receiveMessage(reader);
                    receiveMessage(reader);
                    sendMessage(writer, "AUTH LOGIN");
                    receiveMessage(reader);
                    sendMessage(writer, Base64.getEncoder().encodeToString(SENDER_EMAIL.getBytes()));
                    receiveMessage(reader);
                    sendMessage(writer, Base64.getEncoder().encodeToString(PASSWORD.getBytes()));
                    receiveMessage(reader);
                    sendMessage(writer, "MAIL FROM:<" + SENDER_EMAIL + ">");
                    receiveMessage(reader);
                    sendMessage(writer, "RCPT TO:<" + recipientEmail + ">");
                    receiveMessage(reader);
                    sendMessage(writer, "VRFY");
                    receiveMessage(reader);
                    if (!ccEmail.isEmpty()) {
                        sendMessage(writer, "RCPT TO:<" + ccEmail + ">");
                        receiveMessage(reader);
                        sendMessage(writer, "VRFY");
                        receiveMessage(reader);
                    }
                    sendMessage(writer, "DATA");
                    receiveMessage(reader);

                    // 메일 내용 작성 및 전송
                    writer.write("Subject:" + subject + "\r\n");
                    writer.write("MIME-Version: 1.0\r\n");
                    writer.write("Content-Type: multipart/mixed; boundary=\"boundary\"\r\n");
                    writer.write("\r\n");

                    // 본문 시작
                    writer.write("--boundary\r\n");
                    writer.write("Content-Type: text/plain; charset=\"UTF-8\"\r\n");
                    writer.write("\r\n"); // 헤더와 본문 구분
                    writer.write(content + "\r\n");

                    if (attachmentPath.isEmpty() == false) {
                        writer.write("--boundary\r\n");
                        writer.write("Content-Type: application/octet-stream; name=\"" + fileName + "\"\r\n");
                        writer.write("Content-Transfer-Encoding: base64\r\n");
                        writer.write("Content-Disposition: attachment; filename=\"" + fileName + "\"\r\n");
                        writer.write("\r\n");
                        
                        String encodedFile = Base64.getEncoder().encodeToString(Files.readAllBytes(new File(attachmentPath).toPath()));
                        writer.write(encodedFile + "\r\n");

                        writer.write("\r\n--boundary--\r\n\r\n");
                    }

                    sendMessage(writer, ".");
                    receiveMessage(reader);
                    JOptionPane.showMessageDialog(null,"메일 전송 완료", "전송 성공", JOptionPane.INFORMATION_MESSAGE);
                    // 알림창 -> 메일 보내기 성공

                    receiveMessageAll(reader);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        thread.start();
    }
    public static void main(String[] args) {
        new EmailSenderGUI();
    }

    private static void sendMessage(PrintWriter writer, String message) {
        System.out.println("Send : " + message);
        writer.println(message);
        writer.flush();
    }

    private static void receiveMessage(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        System.out.println("Receive : " + line);
    }
    private static void receiveMessageAll(BufferedReader reader) throws IOException {
        String line;
        while((line = reader.readLine()) != null){
        System.out.println("Receive : " + line);
        }
    }
}

