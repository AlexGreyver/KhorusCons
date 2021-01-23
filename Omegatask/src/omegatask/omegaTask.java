package omegatask;


import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.FilenameFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.*;
import java.util.zip.*;

public class omegaTask {
    public static void main(String[] args) {
        final String dir = System.getProperty("user.dir");
        String ext = ".fb2";
        String extZip = ".fb2.zip";

        File[] zipList = findFiles(dir, extZip);

        //распаковываем зипы
        for (File file : zipList){
            unZip(file);
        }

        //составляем список книг
        File[] fileList = findFiles(dir, ext);

        //сортируем и выводим
        if (fileList.length > 0) {
            List<Book> bookList = getBooks(fileList);
            sortAndOut(bookList);
        }
        else System.out.println("There is no books in that directory");

        //чистим распакованное
        File file = new File(dir);

        File[] listFiles = file.listFiles(new DelFileNameFilter("unzipped"));
        for (File delFile : listFiles){
            delFile.delete();
        }
    }

    private static void unZip(File file){
        try(ZipInputStream zin = new ZipInputStream(new FileInputStream(file)))
            {
                ZipEntry entry;
                String name;

                while((entry=zin.getNextEntry())!=null){

                    name = entry.getName();

                    FileOutputStream fout = new FileOutputStream("unzipped" + name);
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }
                    fout.flush();
                    zin.closeEntry();
                    fout.close();
                }
            }
            catch(Exception ex){

        System.out.println(ex.getMessage());
    } }




    private static File[] findFiles(String dir, String ext) {
        File file = new File(dir);

        File[] listFiles = file.listFiles(new BookFileNameFilter(ext));
        return listFiles;
    }

    public static class BookFileNameFilter implements FilenameFilter{

        private String ext;


        public BookFileNameFilter(String ext){
            this.ext = ext.toLowerCase();

        }
        @Override
        public boolean accept(File dir, String name) {
            return (name.toLowerCase().endsWith(ext));
        }
    }

    public static class DelFileNameFilter implements FilenameFilter{

        private String ext;


        public DelFileNameFilter(String ext){
            this.ext = ext.toLowerCase();

        }
        @Override
        public boolean accept(File dir, String name) {
            return (name.toLowerCase().startsWith(ext));
        }
    }



    private static List<Book> getBooks(File[] files){
        List<Book> bookList = new ArrayList<>();
        for (File file : files){

            bookList.add(new Book(getAuthor(file), getName(file)));
        }
        return bookList;
    }

    private static String getAuthor(File file){
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document dDoc = builder.parse(file);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node firstName = (Node) xPath.evaluate("/FictionBook/description/title-info/author/first-name/text()", dDoc, XPathConstants.NODE);
            Node lastName = (Node) xPath.evaluate("/FictionBook/description/title-info/author/last-name/text()", dDoc, XPathConstants.NODE);

            return (firstName.getNodeValue() + " " + lastName.getNodeValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    private static String getName(File file){
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document dDoc = builder.parse(file);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node bookTitle = (Node) xPath.evaluate("/FictionBook/description/title-info/book-title/text()", dDoc, XPathConstants.NODE);

            return bookTitle.getNodeValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    private static void sortAndOut(List<Book> books){
        List<String> booksToStrings = new ArrayList<>();
        for (Book book : books){
            booksToStrings.add(book.toString());
        }
        Collections.sort(booksToStrings);
        for(String counter: booksToStrings){
            System.out.println(counter);
        }
    }

}

