package storm.starter;

/**
 * Created by fil on 23/11/14.
 */
public class Tester {

    public static void main(String[] args)
    {
        String a = "FLOW,138.37.95.15,105,41,005056bd48c5,333300010002,0x86dd,500,500,fe80:0000:0000:0000:8900:6bd4:95bf:9813,ff02:0000:0000:0000:0000:0000:0001:0002,17,0x00,1,546,547,0x10,181,159,1024";
        String b = "FLOW,138.37.95.24,296,2,001aa034ac8d,0022192f3c15,0x0800,500,500,138.37.88.54,138.37.88.249,6,0x00,64,1232,24561,0x10,70,52,1024";
        String c = "FLOW,138.37.95.25,290,1073741823,005056bd5001,c81f66b16814,0x0806,503,503,-,-,0,0x00,0,0,0,0x00,68,60,1024";
        String d = "FLOW,138.37.95.6,46,27,d4bed99ae5d7,333300010002,0x86dd,0,0,fe80:0000:0000:0000:5caa:dff0:81e1:d4d8,ff02:0000:0000:0000:0000:0000:0001:0002,17,0x00,1,546,547,0x00,182,174,1024";

        String[] arr = new String[4];
        arr[0] = a;
        arr[1] = b;
        arr[2] = c;
        arr[3] = d;

        execute(arr);


    }

    private static void execute(String[] arr) {

        for(String str : arr) {


            String[] elements = str.split(",");

            String srcIP = elements[9];
            String dstIP = elements[10];

            //clears empty cases
            if (srcIP.length() > 1 && dstIP.length() > 1) {
                //extract subnet
                String[] srcBytes = srcIP.split("\\.");
                String[] dstBytes = dstIP.split("\\.");


                String srcSubnet = "";
                String dstSubnet = "";

                //clears IPv6 cases
                if (srcBytes.length != 0 && dstBytes.length != 0) {
                    try {
                        int octets = 3;
                        for (int i = 0; i < octets; i++) {
                            srcSubnet += srcBytes[i] + ".";
                            dstSubnet += dstBytes[i] + ".";

                        }
                        srcSubnet = srcSubnet.substring(0, srcSubnet.length() - 1);
                        dstSubnet = dstSubnet.substring(0, dstSubnet.length() - 1);


                        System.out.println(srcSubnet + "," + dstSubnet);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("out of bounds");
                    }
                } else
                    System.out.println("err,err");
            }
        }

    }

}
