package com.io.readerOrWriter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author: 马士兵教育
 * @create: 2019-09-28 15:21
 */
public class BufferedReaderTest {
    public static void main(String[] args) {

        BufferedReader  reader = null;

        try {
            reader = new BufferedReader(new FileReader("aaa.txt"));
            String read = null;
            while((read = reader.readLine())!=null){
                System.out.println(read);
            };
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
