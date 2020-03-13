package YuangLiu.JunXiao.CS562Project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Start {
    public static void main(String[] args) throws IOException {
        Skyline sk = new Skyline("dataset1.txt");
        while (true) {
            System.out.print("Input 1 for add point, 2 for delete point, 3 for quit: ");
            char line = choice_helper();
            if (line == '3')
                break;
            System.out.print("Input the x value and y value for the point (separate by whitespace): ");
            List<Double> point = point_helper();
            double x = point.get(0);
            double y = point.get(1);
            if (line == '1')
                sk.insert(x, y);
            else if (line == '2')
                sk.delete(x, y);
        }
    }

    public static List<Double> point_helper() {
        List<Double> tmp = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            String[] inter = line.split(" ");
            if (inter.length != 2) {
                System.out.print("Wrong format, input again: ");
                continue;
            }
            try {
                tmp.add(Double.valueOf(inter[0]));
                tmp.add(Double.valueOf(inter[1]));
            }
            catch (NumberFormatException e)  {
                System.out.print("Wrong format, input again: ");
                tmp.clear();
                continue;
            }
            break;
        }
        return tmp;
    }

    public static char choice_helper() {
        Scanner scanner = new Scanner(System.in);
        char tmp;
        while (true) {
            String line = scanner.nextLine();
            if (line.length() != 1) {
                System.out.print("Wrong format, input again: ");
                continue;
            }
            tmp = line.charAt(0);
            if (tmp != '1' && tmp != '2' && tmp != '3') {
                System.out.print("Wrong format, input again: ");
                continue;
            }
            break;
        }
        return tmp;
    }
}
