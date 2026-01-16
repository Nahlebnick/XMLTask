import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.*;

import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


public class LibraryApp extends JFrame
{
	private final String XSDFile = "library.xsd";
	private final String XMLFile = "library.xml";
	private Document document;
	private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
	public LibraryApp() {
        setTitle("Система управления библиотекой");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        int res = loadXML();
        if (res != 0)
        {
        	if (res == 1)
        	{
        		JOptionPane.showMessageDialog(this, "Ошибка валидации XML. Приложение будет закрыто.", "Error", JOptionPane.ERROR_MESSAGE);
        	}
        	else
        	{
        		JOptionPane.showMessageDialog(this, "Ошибка загрузки XML. Приложение будет закрыто.", "Error", JOptionPane.ERROR_MESSAGE);
        	}            
            System.exit(1);
        }
        
        initComponents();
        refreshTable();
    }

    private void refreshTable()
    {
    	tableModel.setRowCount(0);
    	NodeList nList = document.getElementsByTagName("book");
    	for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                tableModel.addRow(new Object[]{
                        elem.getAttribute("id"),
                        getTagValue("title", elem),
                        getTagValue("author", elem),
                        getTagValue("year", elem),
                        getTagValue("price", elem),
                        getTagValue("category", elem),
                        elem.getAttribute("totalCount"),
                        elem.getAttribute("availableCount")
                });
            }
        }
	}
    
    private String getTagValue(String tag, Element element) {
        NodeList nl = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node n = nl.item(0);
        return n.getNodeValue();
    }

	private void initComponents()
    {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAdd = new JButton("Добавить книгу");
        btnAdd.addActionListener(e -> addBook());
        
        JButton btnPrice = new JButton("Переоценка");
        btnPrice.addActionListener(e -> changePrice());
        
        JButton btnCheckout = new JButton("Выдать книгу");
        btnCheckout.addActionListener(e -> checkoutBook());

        JLabel lblSearch = new JLabel("Поиск:");
        searchField = new JTextField(20);
        JButton btnSearch = new JButton("Найти");
        JButton btnReset = new JButton("Сброс");

        controlPanel.add(btnAdd);
        controlPanel.add(btnPrice);
        controlPanel.add(btnCheckout);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(lblSearch);
        controlPanel.add(searchField);
        controlPanel.add(btnSearch);
        controlPanel.add(btnReset);

        add(controlPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Название", "Автор", "Год", "Цена", "Категория", "Всего", "В наличии"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
	
	private void changePrice() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите книгу в таблице!");
            return;
        }

        String currentPrice = (String) table.getValueAt(selectedRow, 4);
        String id = (String) table.getValueAt(selectedRow, 0);
        String newPrice = JOptionPane.showInputDialog(this, "Текущая цена: " + currentPrice + "\nВведите новую цену:");

        try {
        	double price = Double.parseDouble(newPrice);
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(this, "Invalid price passed!", "Error", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        if (newPrice != null && !newPrice.isEmpty()) {
            updateBookField(id, "price", newPrice);
            saveXML();
            refreshTable();
        }
    }
	
	private void updateBookField(String id, String tagName, String newValue) {
        Node bookNode = findBookNodeById(id);
        if (bookNode != null) {
            Element elem = (Element) bookNode;
            Node fieldNode = elem.getElementsByTagName(tagName).item(0);
            fieldNode.setTextContent(newValue);
        }
    }
	
	private Node findBookNodeById(String id) {
        NodeList nList = document.getElementsByTagName("book");
        for (int i = 0; i < nList.getLength(); i++) {
            Element elem = (Element) nList.item(i);
            if (elem.getAttribute("id").equals(id)) {
                return elem;
            }
        }
        return null;
    }

	private void addBook()
	{
		JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField catField = new JTextField();
        JTextField idField = new JTextField();
        JTextField totalField = new JTextField();

        Object[] message = {
                "ID:", idField,
                "Title:", titleField,
                "Author:", authorField,
                "Year:", yearField,
                "Price:", priceField,
                "Category:", catField,
                "Total count:", totalField
        };
        System.out.print(11);
        int option = JOptionPane.showConfirmDialog(null, message, "Добавить книгу", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Element root = document.getDocumentElement();
                Element newBook = document.createElement("book");

                newBook.setAttribute("id", idField.getText());
                newBook.setAttribute("totalCount", totalField.getText());
                newBook.setAttribute("availableCount", totalField.getText());
                
                newBook.appendChild(getElem("title", titleField.getText()));
                newBook.appendChild(getElem("author", authorField.getText()));
                newBook.appendChild(getElem("year", yearField.getText()));
                newBook.appendChild(getElem("price", priceField.getText()));
                newBook.appendChild(getElem("category", catField.getText()));

                root.appendChild(newBook);
                saveXML();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка ввода данных");
            }
        }
	}
	
	private Element getElem(String tag, String content)
	{
		 Element el = document.createElement(tag);
         el.appendChild(document.createTextNode(content));
         return el;
	}
	
	private void checkoutBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите книгу для выдачи!");
            return;
        }

        String id = (String) table.getValueAt(selectedRow, 0);
        Node bookNode = findBookNodeById(id);
        
        if (bookNode != null) {
            Element bookElem = (Element) bookNode;
            int available = Integer.parseInt(bookElem.getAttribute("availableCount"));
            
            if (available > 0) {
                bookElem.setAttribute("availableCount", String.valueOf(available - 1));
                saveXML();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Книга выдана успешно.");
            } else {
                JOptionPane.showMessageDialog(this, "Нет доступных экземпляров!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
	
	private void saveXML() {
		 try {
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            
	            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            DOMSource source = new DOMSource(document);
	            StreamResult result = new StreamResult(new File(XMLFile));
	            transformer.transform(source, result);
	        } catch (TransformerException e) {
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(this, "Ошибка сохранения файла!");
	        }		
	}

	private int loadXML() {
        try 
        {
        	validateXML();
        	
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            document = dBuilder.parse(new File(XMLFile));
            document.getDocumentElement().normalize();
            return 0;

        } 
        catch (SAXException e) {
            System.out.println("XML file is not valid");
            return 1;
        } 
        catch (IOException | ParserConfigurationException e)
        {
        	e.printStackTrace();
        	return 2;
        }
    }
	
	private int validateXML() throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema sc = factory.newSchema(new File(XSDFile));
            
        Validator validator = sc.newValidator();
        Source xmlSource = new StreamSource(new File(XMLFile));
        validator.validate(xmlSource);
        return 0;    
	}
}