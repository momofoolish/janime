import cn.hutool.crypto.SmUtil;

public class Test {

    public static void main(String[] args) {
        String s = SmUtil.sm3WithSalt("janime@salt".getBytes()).digestHex("janime@pwd");
        System.out.println(s.toUpperCase());
    }

}
