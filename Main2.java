import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


// DATABASE
class FileDataBase {
    static final Path FILE_PATH = Paths.get("book.txt");

    public static List<Library> loadBooks() {
        List<Library> books = new ArrayList<>();

        try {
            if (!Files.exists(FILE_PATH)) {
                Files.createFile(FILE_PATH);
            }

            try (BufferedReader reader =
                     Files.newBufferedReader(FILE_PATH)) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] data = line.split(",", -1);
                    if (data.length != 4) continue;

                    try {
                        Library book = new NewBook(
                            data[0].trim(),
                            Integer.parseInt(data[1].trim()),
                            data[2].trim()
                        );

                        book.setisBorrowed(
                            Boolean.parseBoolean(data[3].trim())
                        );
                        books.add(book);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid book record.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return books;
    }

    public static void saveBooks(List<Library> books) {
        try (BufferedWriter writer =
                 Files.newBufferedWriter(FILE_PATH)) {
            for (Library book : books) {
                writer.write(
                    book.getAuthor() + "," +
                    book.getId() + "," +
                    book.getTitle() + "," +
                    book.getIsBorrowed()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }
}


// PARENT CLASS
abstract class Library {
    private int id;
    private String title;
    private String author;
    private boolean isBorrowed;

    public Library(String author, int id, String title) {
        this.author = author;
        this.id = id;
        this.title = title;
        this.isBorrowed = false;
    }

    public String getAuthor() {
        return author;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean getIsBorrowed() {
        return isBorrowed;
    }

    public void setisBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }

    public void libraryBorrow() {
        isBorrowed = true;
        System.out.println(title + " borrowed successfully.");
    }

    public void libraryReturn() {
        isBorrowed = false;
        System.out.println(title + " returned successfully.");
    }

    public void displayInfo() {
        System.out.println("Author: " + author);
        System.out.println("ID: " + id);
        System.out.println("Title: " + title);
        System.out.println("Available: " + !isBorrowed);
    }
}


// CHILD CLASSES
class NewBook extends Library {
    public NewBook(String author, int id, String title) {
        super(author, id, title);
    }
}

class PrintedBook extends Library {
    public PrintedBook(String author, int id, String title) {
        super(author, id, title);
    }

    @Override
    public void displayInfo() {
        System.out.println("\n--- Printed Book ---");
        super.displayInfo();
        System.out.println("Type: Physical Book");
    }
}

class Ebook extends Library {
    public Ebook(String author, int id, String title) {
        super(author, id, title);
    }

    @Override
    public void displayInfo() {
        System.out.println("\n--- E-Book ---");
        super.displayInfo();
        System.out.println("Type: Electronic Book");
    }
}


// GUI
class LibraryGUI extends JFrame {
    private JTextField idField;
    private JLabel statusLabel;
    private final List<Library> books;

    public LibraryGUI(List<Library> books) {
        this.books = books;

        setTitle("DEIHS Library System");
        setSize(500, 320);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(
            new Color(240, 244, 249)
        );

        JLabel heading = new JLabel("DEIHS Library System");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(new Color(22, 54, 92));
        heading.setBounds(30, 15, 400, 35);
        add(heading);

        JLabel label = new JLabel("Enter Book ID:");
        label.setBounds(30, 65, 100, 30);
        add(label);

        idField = new JTextField();
        idField.setBounds(130, 65, 300, 30);
        add(idField);

        JButton borrowButton = new JButton("Borrow");
        borrowButton.setBounds(30, 115, 130, 40);
        add(borrowButton);

        JButton returnButton = new JButton("Return");
        returnButton.setBounds(175, 115, 130, 40);
        add(returnButton);

        JButton searchButton = new JButton("Search");
        searchButton.setBounds(320, 115, 110, 40);
        add(searchButton);

        JButton uploadButton = new JButton("Upload Book");
        uploadButton.setBounds(150, 170, 170, 35);
        add(uploadButton);

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setBounds(30, 225, 440, 30);
        add(statusLabel);

        borrowButton.addActionListener(e -> processBook(1));
        returnButton.addActionListener(e -> processBook(2));
        searchButton.addActionListener(e -> processBook(3));
        uploadButton.addActionListener(e -> uploadBook());
    }

    private void processBook(int action) {
        final int id;

        try {
            id = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Enter a valid Book ID.");
            return;
        }

        statusLabel.setText("Processing...");

        setButtonsEnabled(false);

        Thread task = new Thread(() -> {
            String message;

            try {
                Thread.sleep(500);

                Library found = null;

                for (Library book : books) {
                    if (book.getId() == id) {
                        found = book;
                        break;
                    }
                }

                if (found == null) {
                    message = "Book not found.";
                } else if (action == 1) {
                    if (found.getIsBorrowed()) {
                        message = "Book is already borrowed.";
                    } else {
                        found.libraryBorrow();
                        FileDataBase.saveBooks(books);
                        message = "Borrowed: " + found.getTitle();
                    }
                } else if (action == 2) {
                    if (!found.getIsBorrowed()) {
                        message = "Book is not borrowed.";
                    } else {
                        found.libraryReturn();
                        FileDataBase.saveBooks(books);
                        message = "Returned: " + found.getTitle();
                    }
                } else {
                    message = found.getTitle()
                        + " | Author: " + found.getAuthor()
                        + " | Available: "
                        + !found.getIsBorrowed();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                message = "Task interrupted.";
            }

            final String result = message;
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(result);
                setButtonsEnabled(true);
            });
        });

        task.start();
    }

    private void setButtonsEnabled(boolean enabled) {
        for (Component component : getContentPane().getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(enabled);
            }
        }
    }

    private void uploadBook() {
        JTextField authorField = new JTextField();
        JTextField bookIdField = new JTextField();
        JTextField titleField = new JTextField();

        String[] types = {"Printed Book", "E-Book"};
        JComboBox<String> typeBox = new JComboBox<>(types);

        Object[] fields = {
            "Author:", authorField,
            "Book ID:", bookIdField,
            "Book Title:", titleField,
            "Book Type:", typeBox
        };

        int result = JOptionPane.showConfirmDialog(
            this, fields, "Upload Book",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        String author = authorField.getText().trim();
        String title = titleField.getText().trim();
        int id;

        try {
            id = Integer.parseInt(bookIdField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Book ID must be a number.");
            return;
        }

        if (author.isEmpty() || title.isEmpty()
            || author.contains(",") || title.contains(",")) {
            statusLabel.setText("Enter valid book details.");
            return;
        }

        if (Main2.bookIdExists(books, id)) {
            statusLabel.setText("Book ID already exists.");
            return;
        }

        Library book;

        if (typeBox.getSelectedIndex() == 0) {
            book = new PrintedBook(author, id, title);
        } else {
            book = new Ebook(author, id, title);
        }

        books.add(book);
        FileDataBase.saveBooks(books);
        statusLabel.setText("Book uploaded successfully.");
    }
}


// MAIN CLASS
public class Main2 {
    public static boolean bookIdExists(
        List<Library> books, int id
    ) {
        for (Library book : books) {
            if (book.getId() == id) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        List<Library> books = FileDataBase.loadBooks();

        Library[] defaults = {
            new PrintedBook("rene", 10, "MAMAA"),
            new Ebook("Renevive", 11, "Wheres Mama")
        };

        for (Library book : defaults) {
            if (!bookIdExists(books, book.getId())) {
                books.add(book);
            }
        }

        FileDataBase.saveBooks(books);

        SwingUtilities.invokeLater(() -> {
            LibraryGUI gui = new LibraryGUI(books);
            gui.setVisible(true);
        });

        try (Scanner input = new Scanner(System.in)) {
            while (true) {
                System.out.println("\nDEIHS Library System");
                System.out.println("1. Borrow Book");
                System.out.println("2. Return Book");
                System.out.println("3. Search Book");
                System.out.println("4. Upload Book");
                System.out.println("5. Quit");
                System.out.print("Enter choice: ");

                int choice;

                try {
                    choice = Integer.parseInt(
                        input.nextLine().trim()
                    );
                } catch (NumberFormatException e) {
                    System.out.println("Invalid choice.");
                    continue;
                }

                if (choice == 5) {
                    FileDataBase.saveBooks(books);
                    System.out.println("Thank you!");
                    break;
                }

                if (choice == 4) {
                    uploadConsoleBook(input, books);
                    continue;
                }

                if (choice < 1 || choice > 3) {
                    System.out.println("Invalid choice.");
                    continue;
                }

                System.out.print("Enter Book ID: ");
                int id;

                try {
                    id = Integer.parseInt(
                        input.nextLine().trim()
                    );
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID.");
                    continue;
                }

                Library found = null;

                for (Library book : books) {
                    if (book.getId() == id) {
                        found = book;
                        break;
                    }
                }

                if (found == null) {
                    System.out.println("Book not found.");
                    continue;
                }

                if (choice == 1) {
                    if (found.getIsBorrowed()) {
                        System.out.println("Already borrowed.");
                    } else {
                        found.libraryBorrow();
                    }
                } else if (choice == 2) {
                    if (!found.getIsBorrowed()) {
                        System.out.println("Not borrowed.");
                    } else {
                        found.libraryReturn();
                    }
                } else {
                    found.displayInfo();
                }

                FileDataBase.saveBooks(books);
            }
        }
    }

    public static void uploadConsoleBook(
        Scanner input, List<Library> books
    ) {
        System.out.print("Enter Author: ");
        String author = input.nextLine().trim();

        System.out.print("Enter Book ID: ");
        int id;

        try {
            id = Integer.parseInt(input.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        if (author.isEmpty() || author.contains(",")
            || bookIdExists(books, id)) {
            System.out.println("Invalid author or duplicate ID.");
            return;
        }

        System.out.print("Enter Book Title: ");
        String title = input.nextLine().trim();

        if (title.isEmpty() || title.contains(",")) {
            System.out.println("Invalid title.");
            return;
        }

        System.out.print("Type (1 Printed, 2 E-Book): ");
        String type = input.nextLine().trim();

        Library book;

        if (type.equals("1")) {
            book = new PrintedBook(author, id, title);
        } else if (type.equals("2")) {
            book = new Ebook(author, id, title);
        } else {
            System.out.println("Invalid type.");
            return;
        }

        books.add(book);
        FileDataBase.saveBooks(books);
        System.out.println("Book uploaded successfully.");
    }
}