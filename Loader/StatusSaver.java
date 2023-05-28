package Loader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import Data.Status;

public class StatusSaver {
    public static void saveStatusToFile(ArrayList<Status> statusList, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Status status : statusList) {
                writer.write(status.toString());
                writer.newLine();
            }
            System.out.println("Status saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
