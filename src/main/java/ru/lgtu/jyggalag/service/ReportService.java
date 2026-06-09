package ru.lgtu.jyggalag.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.List;
import ru.lgtu.jyggalag.model.Task;
import ru.lgtu.jyggalag.model.Note;

/**
 * Генерация отчётов в PDF с использованием iText 5
 */
public class ReportService {

    /**
     * Вспомогательный метод для настройки шрифта с поддержкой кириллицы
     */
    private Font getFont(float size, int style) {
        try {
            String fontPath = "C:/Windows/Fonts/arial.ttf";
            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(bf, size, style);
        } catch (Exception e) {

            return new Font(Font.FontFamily.HELVETICA, size, style);
        }
    }


    public void generateTasksReport(List<Task> tasks) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("tasks_report.pdf"));
        document.open();

        Font titleFont = getFont(16, Font.BOLD);
        Font normalFont = getFont(12, Font.NORMAL);
        Font headerFont = getFont(12, Font.BOLD);


        document.add(new Paragraph("ОТЧЕТ ПО ЗАДАЧАМ ПОЛЬЗОВАТЕЛЯ", titleFont));
        document.add(new Paragraph("Всего задач в планировщике: " + tasks.size(), normalFont));
        document.add(new Paragraph(" ", normalFont));


        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);


        table.addCell(new PdfPCell(new Phrase("Название задачи", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Статус", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Приоритет", headerFont)));


        for (Task task : tasks) {
            String title = task.getTitle() != null ? task.getTitle() : "—";
            String status = task.getStatus() != null ? task.getStatus().name() : "—";
            String priority = task.getPriority() != null ? task.getPriority().name() : "—";

            table.addCell(new PdfPCell(new Phrase(title, normalFont)));
            table.addCell(new PdfPCell(new Phrase(status, normalFont)));
            table.addCell(new PdfPCell(new Phrase(priority, normalFont)));
        }

        document.add(table);
        document.close();
    }
    
    public void generateNotesReport(List<Note> notes) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("notes_report.pdf"));
        document.open();

        Font titleFont = getFont(16, Font.BOLD);
        Font normalFont = getFont(12, Font.NORMAL);
        Font boldFont = getFont(12, Font.BOLD);
        Font smallFont = getFont(11, Font.NORMAL);
        
        document.add(new Paragraph("ОТЧЕТ ПО ЗАМЕТКАМ ПОЛЬЗОВАТЕЛЯ", titleFont));
        document.add(new Paragraph("Всего сохраненных заметок: " + notes.size(), normalFont));
        document.add(new Paragraph(" ", normalFont));
        
        for (Note note : notes) {
            String title = (note.getTitle() != null && !note.getTitle().isEmpty()) ? note.getTitle() : "Без названия";
            document.add(new Paragraph("• Заметка: " + title, boldFont));

            if (note.getContent() != null && !note.getContent().isEmpty()) {
                String preview = note.getContent().length() > 120 ? note.getContent().substring(0, 120) + "..." : note.getContent();
                document.add(new Paragraph("  Содержимое: " + preview, smallFont));
            }
            document.add(new Paragraph("------------------------------------------------------------------", getFont(10, Font.NORMAL)));
        }

        document.close();
    }
}