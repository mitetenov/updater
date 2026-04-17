package com.remnaupdater.app.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileSubscribersRepository {
    private final Map<String, String> data = new HashMap<>();

    public FileSubscribersRepository(String filePath) {
        this(filePath, StandardCharsets.UTF_8);
    }

    public FileSubscribersRepository(String filePath, Charset charset) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Путь к файлу не может быть пустым");
        }
        try {
            if (filePath.toLowerCase().endsWith(".xlsx") || filePath.toLowerCase().endsWith(".xls")) {
                loadXlsx(filePath);
            } else {
                loadCsv(filePath, charset == null ? StandardCharsets.UTF_8 : charset);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения файла: " + filePath, e);
        }
    }

    private void loadXlsx(String filePath) throws IOException {
        System.out.println("[FileSubscribersRepository] Загрузка Excel: " + filePath);
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {
             
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return;
            
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return;

            int followerIdx = -1;
            int fromIdx = -1;

            for (Cell cell : headerRow) {
                String val = getCellValueAsString(cell);
                if (val != null) {
                    val = val.trim();
                    if ("Follower ID".equals(val)) followerIdx = cell.getColumnIndex();
                    else if ("From".equals(val)) fromIdx = cell.getColumnIndex();
                }
            }

            if (followerIdx == -1 || fromIdx == -1) {
                throw new IllegalStateException("Не найдены столбцы 'Follower ID' или 'From' в Excel");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell followerCell = row.getCell(followerIdx);
                Cell fromCell = row.getCell(fromIdx);

                if (followerCell != null) {
                    String followerId = getCellValueAsString(followerCell).trim();
                    if (!followerId.isEmpty()) {
                        String from = fromCell != null ? getCellValueAsString(fromCell).trim() : "";
                        if (!from.isEmpty()) {
                            data.put(followerId, from);
                        }
                    }
                }
            }
        }
        System.out.println("[FileSubscribersRepository] Загружено записей из Excel: " + data.size());
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: 
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            default: return cell.toString();
        }
    }

    private void loadCsv(String csvPath, Charset charset) throws IOException, CsvValidationException {
        System.out.println("[FileSubscribersRepository] Загрузка CSV: " + csvPath);
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .withIgnoreQuotations(false)
                .build();
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(csvPath), charset))
                .withCSVParser(parser)
                .withSkipLines(0)
                .build()) {
            String[] headers = reader.readNext();
            if (headers == null) return;
            
            int followerIdx = -1;
            int fromIdx = -1;
            
            for (int i = 0; i < headers.length; i++) {
                String key = headers[i] == null ? null : headers[i].trim();
                if ("Follower ID".equals(key)) followerIdx = i;
                else if ("From".equals(key)) fromIdx = i;
            }
            if (followerIdx == -1 || fromIdx == -1) {
                throw new IllegalStateException("Не найдены столбцы 'Follower ID' или 'From' в CSV");
            }
            
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length <= Math.max(followerIdx, fromIdx)) {
                    continue;
                }
                String followerId = row[followerIdx];
                if (followerId != null) {
                    followerId = followerId.trim();
                    if (!followerId.isEmpty()) {
                        String from = row[fromIdx];
                        if (from != null && !from.trim().isEmpty()) {
                            data.put(followerId, from.trim());
                        }
                    }
                }
            }
        }
        System.out.println("[FileSubscribersRepository] Загружено записей из CSV: " + data.size());
    }

    public Optional<String> findFromByFollowerId(String followerId) {
        System.out.println("[FileSubscribersRepository] Поиск From по Follower ID = " + followerId);
        return Optional.ofNullable(data.get(followerId));
    }
}
