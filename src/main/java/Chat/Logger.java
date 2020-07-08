package Chat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Logger {
    private final int BUFFER_SIZE = 50; // Размер буфера строк, которые будут висеть в памяти до записи в файл
    private final int BUFFER_WRITE_LIMIT = 35; // Количество строк в буфере, при котором будет происходить запись в файл
    private String[] writeBuffer; //Строки, которые будут висеть в памяти (чтобы не записывать в файл по одной штуке)
    int bufferSize; // Переменная будет следить за количеством уже имеющихся в буфере строк
    private final String LOG_FILE_NAME = "log.txt"; // Файл лога
    public Logger() {
        this.bufferSize = 0;
        this.writeBuffer = new String[BUFFER_SIZE];
        System.out.println("Create Logger");
    }

    /**
     * Занесение сообщения в лог с последующим сохранением в файл по мере заполнения входного буфера
     * @param message Сохраняемое сообщение
     */
    public synchronized void write(String message) {
        // Добавление даты и времени к логируемому событию
        writeBuffer[bufferSize] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                + " * " + message
                + System.lineSeparator();
        bufferSize++;
        // Если буфер близок к переполнению, выполняется перенос его содержимого в файл
        if (bufferSize == BUFFER_WRITE_LIMIT) {
            writeIntoFile();
        }
    }

    /**
     * Чтение заданного количества последних записей из файла лога
     * @param countOfRecords Требуемое количество последних записей
     */
    public synchronized void read(int countOfRecords) {
        List<String> readBuffer = new ArrayList<>(); // Буфер считываемых записей
        // Определение размера файла и приблизительного начального смещения для чтения из расчёта в среднем 100 байт на строку
        File logFile = new File(LOG_FILE_NAME);
        long seek = logFile.length() - 100 * countOfRecords;
        if (seek < 0) { seek = 0; }
        // Считывание из файла в буфер заведомо большего числа строк
        try (RandomAccessFile file = new RandomAccessFile(LOG_FILE_NAME, "r")){
            file.seek(seek);
            String record;
            while (true) {
                record = file.readLine();
                if (record == null) { break; }
                readBuffer.add(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Выводим из буфера только необходимое число последних строк
        int start = readBuffer.size() - countOfRecords; // Запись в буфере, с которой начинаем вывод на консоль
        if (start < 0) { start = 0; } // Либо с начала буфера, если было считано строк меньше необходимого
        for (int i = start; i < readBuffer.size(); i++) {
            System.out.println(readBuffer.get(i));
        }
    }

    /**
     * Сохранение буфера входящих сообщений в файл
     */
    private void writeIntoFile() {
        try (FileWriter fileWriter = new FileWriter(LOG_FILE_NAME, true)) {
            for (int i = 0; i < bufferSize; i++) {
                fileWriter.write(writeBuffer[i]);
            }
            bufferSize = 0; // После сохранения начинаем запись в буфер с начала. Принимаем его размер равным нулю
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Действия, выполняемые перед закрытием логгера
     */
    public void stop() {
        writeIntoFile(); // Если во входном буфере остались не сохранённые записи, дописываем их в файл.
        System.out.println("Logger stopped");
    }
}
