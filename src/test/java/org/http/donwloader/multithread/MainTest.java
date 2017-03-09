package org.http.donwloader.multithread;

import org.junit.Test;

public class MainTest {

    @Test
    public void mainTest() throws Exception {
        String[] input = {"-n", "5", "-l", "500m", "-o", "output_folder", "-f", "C:\\git\\miltithread-donwloader\\src\\test\\resources\\links.txt"};
        Main.main(input);
    }
}