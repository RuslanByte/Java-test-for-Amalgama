package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                throw new IllegalArgumentException("Неизвестный тип данных в ячейке: " + cell.getCellType());
        }
    }

    private static int extractNumber(String name) {
        String[] parts = name.split("№");
        if (parts.length > 1) {
            try {
                return Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static void main(String[] args) throws Exception {
        String excelFilePath = "/home/ruslan/example.xlsx";

        Factory factory = null;

        try (FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath))) {
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            Sheet scenarioSheet = workbook.getSheet("Scenario");
            if (scenarioSheet == null) {
                throw new IllegalArgumentException("Лист 'Scenario' не найден в файле Excel");
            }
            Row rowScenario = scenarioSheet.getRow(2);
            if (rowScenario == null) {
                throw new IllegalArgumentException("Строка 1 отсутствует в листе 'Scenario'");
            }

            int workersCount;
            int detailsCount;

            Cell workersCell = rowScenario.getCell(0);
            if (workersCell.getCellType() == CellType.NUMERIC) {
                workersCount = (int) workersCell.getNumericCellValue();
            } else if (workersCell.getCellType() == CellType.STRING) {
                workersCount = Integer.parseInt(workersCell.getStringCellValue().trim());
            } else {
                throw new IllegalStateException("Неподдерживаемый тип данных в ячейке A2 (Scenario)");
            }

            Cell detailsCell = rowScenario.getCell(1);
            if (detailsCell.getCellType() == CellType.NUMERIC) {
                detailsCount = (int) detailsCell.getNumericCellValue();
            } else if (detailsCell.getCellType() == CellType.STRING) {
                detailsCount = Integer.parseInt(detailsCell.getStringCellValue().trim());
            } else {
                throw new IllegalStateException("Неподдерживаемый тип данных в ячейке B2 (Scenario)");
            }

            Sheet productionCenterSheet = workbook.getSheet("ProductionCenter");
            if (productionCenterSheet == null) {
                throw new IllegalArgumentException("Лист 'ProductionCenter' не найден в файле Excel");
            }

            Map<String, ProductionCenter> productionCenters = new HashMap<>();
            for (int i = 2; i <= productionCenterSheet.getLastRowNum(); i++) {
                Row row = productionCenterSheet.getRow(i);
                if (row == null) continue;
                String id = getCellValueAsString(row.getCell(0));
                String name = getCellValueAsString(row.getCell(1));
                double performance = Double.parseDouble(getCellValueAsString(row.getCell(2)));
                int maxWorkersCount = (int) Double.parseDouble(getCellValueAsString(row.getCell(3)));
                productionCenters.put(id, new ProductionCenter(id, name, performance, maxWorkersCount));
            }

            Sheet connectionSheet = workbook.getSheet("Connection");
            if (connectionSheet == null) {
                throw new IllegalArgumentException("Лист 'Connection' не найден в файле Excel");
            }

            for (int i = 2; i <= connectionSheet.getLastRowNum(); i++) {
                Row row = connectionSheet.getRow(i);
                if (row == null) continue;
                String sourceId = getCellValueAsString(row.getCell(0));
                String destId = getCellValueAsString(row.getCell(1));

                ProductionCenter sourceCenter = productionCenters.get(sourceId);
                ProductionCenter destCenter = productionCenters.get(destId);

                if (sourceCenter == null || destCenter == null) {
                    throw new IllegalArgumentException("Некорректные данные в таблице 'Connection': " +
                            "sourceId=" + sourceId + ", destId=" + destId);
                }

                sourceCenter.addNextCenter(destCenter);
            }

            workbook.close();

            List<ProductionCenter> sortedCenters = productionCenters.values().stream()
                    .sorted((pc1, pc2) -> {
                        int number1 = extractNumber(pc1.getName());
                        int number2 = extractNumber(pc2.getName());
                        return Integer.compare(number1, number2);
                    })
                    .collect(Collectors.toList());

            System.out.println("Отсортированные производственные центры:");
            sortedCenters.forEach(center -> System.out.println(center.getName()));

            factory = new Factory(
                    new ArrayList<>(
                            productionCenters.values().stream()
                                    .sorted((pc1, pc2) -> {
                                        int number1 = extractNumber(pc1.getName());
                                        int number2 = extractNumber(pc2.getName());
                                        return Integer.compare(number1, number2);
                                    })
                                    .collect(Collectors.toList())
                    ),
                    workersCount,
                    detailsCount
            );

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла Excel: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка в данных Excel: " + e.getMessage());
            e.printStackTrace();
        }

        if (factory != null) {
            double totalSimulationTime = 60.0;
            factory.simulate(totalSimulationTime);
            System.out.println("Симуляция завершена!");
        } else {
            System.err.println("Симуляция не запущена из-за ошибок.");
        }
    }
}
