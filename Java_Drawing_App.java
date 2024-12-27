package java_drawing_app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author 1BestCsharp
 */
public class Java_Drawing_App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Drawing APP");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        PaintPanel paintPanel = new PaintPanel();
        frame.add(paintPanel, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        JButton clearButton = createButton("Clear Canvas", Color.red);
        JButton colorButton = createButton("Choose Color", new Color(63,81,181));
        JButton[] shapeButtons = {createButton("Freehand", new Color(211,84,0)),
                                  createButton("Rectangle", new Color(142,68,173)),
                                  createButton("Oval", new Color(39,174,96)),
                                };
    
        JButton saveButton = createButton("Save Image", new Color(20,10,00));
        
        controlPanel.add(clearButton);
        controlPanel.add(colorButton);
        for(JButton button : shapeButtons){
            controlPanel.add(button);
        }
        controlPanel.add(saveButton);
        
        frame.add(controlPanel, BorderLayout.NORTH);
        
        // Add action listeners for buttons
        colorButton.addActionListener((e) -> { setColor(paintPanel, frame); });
        clearButton.addActionListener((e) -> { paintPanel.clearCanvas(); });
        saveButton.addActionListener((e) -> { saveImage(paintPanel); });
        
        for(int i = 0; i < shapeButtons.length; i++){
            final int index = i;
            shapeButtons[i].addActionListener(e->paintPanel.setCurrentShapeType(ShapeType.values()[index]));
        }
        

        frame.setVisible(true);
    
        
    }
    
    // Method to create buttons with custom styling
    private static JButton createButton(String text, Color bgColor)
    {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    // Method to choose and set the drawing color
    private static void setColor(PaintPanel paintPanel, JFrame frame)
    {
        Color color = JColorChooser.showDialog(frame, "Choose Color", paintPanel.getCurrentColor());
        
        if(color != null)
        {
            paintPanel.setCurrentColor(color);
        }
    }
    
    
    // Method to save the current drawing as an image file
    private static void saveImage(PaintPanel paintPanel){
        BufferedImage image = new BufferedImage(paintPanel.getWidth(), paintPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        paintPanel.paint(image.getGraphics());
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        int userSelection = fileChooser.showSaveDialog(paintPanel);
        if(userSelection == JFileChooser.APPROVE_OPTION){
            File fileToSave = fileChooser.getSelectedFile();
            
            try{
                
                ImageIO.write(image, "PNG", fileToSave);
                JOptionPane.showMessageDialog(paintPanel, "Image Saved Successfully");
                
            }catch(IOException ex){
                JOptionPane.showMessageDialog(paintPanel, "Error Saving The Image: " + ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
            }
            
        }
        
        
    }
    
    
 }   
    
    class PaintPanel extends JPanel
    {
        private List<Shape> shapes;
        private Color currentColor;
        private ShapeType currentShapeType;
        
        public PaintPanel(){
        
            shapes = new ArrayList<>();
            currentColor = Color.BLACK;
            currentShapeType = ShapeType.FREEHAND;
            setBackground(Color.WHITE);
            
            // Mouse listeners for drawing shapes
            addMouseListener(new MouseAdapter() {
                
                @Override
                public void mousePressed(MouseEvent e)
                {
                    Point startPoint = e.getPoint();
                    Shape shape;
                    // Create a new shape based on the selected shape type
                    shape = switch(currentShapeType){
                        case FREEHAND -> new FreehandShape(currentColor,startPoint);
                        case RECTANGLE -> new RectangleShape(currentColor,startPoint);
                        case OVAL -> new OvalShape(currentColor,startPoint);
                        default -> new FreehandShape(currentColor,startPoint);
                    };
                    
                    shapes.add(shape);
                    repaint();
                }
                
            });
            
            
            // Mouse motion listener for freehand drawing
            addMouseMotionListener(new MouseAdapter() {
                
                @Override
                public void mouseDragged(MouseEvent e)
                {
                    if(!shapes.isEmpty())
                    {
                        Point endPoint = e.getPoint();
                        shapes.get(shapes.size() - 1).addPoint(endPoint);
                        repaint();
                    }
                }
                
            });
            
        }
        
        // Set the current drawing color
        public void setCurrentColor(Color color) { currentColor = color; }
        
        // Get the current drawing color
        public Color getCurrentColor(){ return currentColor; }
        
        // Set the current shape type (freehand, rectangle, oval)
        public void setCurrentShapeType(ShapeType shapeType){ 
            currentShapeType = shapeType; 
        }
        
        // Clear the canvas by removing all shapes
        public void clearCanvas(){ 
            shapes.clear();
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            // Draw all shapes on the canvas
            for(Shape shape : shapes){
                shape.draw(g);
            }
        }
        
    }


   // Interface for different shapes 
    interface Shape
    {
        void draw(Graphics g);
        void addPoint(Point point);
    }
    
    // Enumeration for shape types
    enum ShapeType{
        FREEHAND,
        RECTANGLE,
        OVAL
    };
    
    
    // Classes for specific shapes (Freehand, Rectangle, Oval)
    class FreehandShape implements Shape{
        private Color color;
        private List<Point> points;

        // Constructor for FreehandShape class
        public FreehandShape(Color color, Point startPoint){
            // Initialize the shape's color with the provided color
            this.color = color;
            // Create a list to store the points of the shape
            points = new ArrayList<>();
            // Add the initial point (startPoint) to the list
            points.add(startPoint);
        }
        
        @Override // Method to draw a FreehandShape
        public void draw(Graphics g) {
        
            // Set the drawing color to the shape's color
            g.setColor(color);
            // Iterate through the list of points to draw lines between them
            for(int i = 1; i < points.size(); i++){
                Point startPoint = points.get(i -1);
                Point endPoint = points.get(i);
                // Draw a line between consecutive points
                g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            }
            
        }

        @Override // Method to add a new point to the FreehandShape
        public void addPoint(Point point) {
        
            // Add the provided point to the list of points, extending the shape
            points.add(point);
        
        }
    }
    
    
    
    class RectangleShape implements Shape{

        private Color color;
        private Point startPoint;
        private Point endPoint;
        
        // Constructor for RectangleShape class
        public RectangleShape(Color color, Point startPoint){
            // Initialize the shape's color with the provided color
            this.color = color;
            // Initialize both the starting and ending points to the same point (startPoint)
            this.startPoint = startPoint;
            this.endPoint = startPoint;
        }
        
        
        @Override
        public void draw(Graphics g) {
            // Set the drawing color to the shape's color
            g.setColor(color);
            // Calculate the coordinates and dimensions of the rectangle
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            
            int width = Math.abs(startPoint.x - endPoint.x);
            int height = Math.abs(startPoint.y - endPoint.y);
            // Draw a rectangle with the calculated properties
            g.drawRect(x, y, width, height);
            
        }

        @Override // Method to update the ending point of the RectangleShape
        public void addPoint(Point point) {
            // Update the ending point to the provided point (used for resizing the rectangle)
            endPoint = point;
        }
        
    }
    
    
    class OvalShape implements Shape
    {
        private Color color;
        private Point startPoint;
        private Point endPoint;

        // Constructor for OvalShape class
        public OvalShape(Color color, Point startPoint){
            
            // Initialize the shape's color with the provided color
            this.color = color;
            // Initialize both the starting and ending points to the same point (startPoint)
            this.startPoint = startPoint;
            this.endPoint = startPoint;
            
        }
        
        
        @Override // Method to draw an OvalShape
        public void draw(Graphics g) {
        
            // Set the drawing color to the shape's color
            g.setColor(color);
            // Calculate the coordinates and dimensions of the oval
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            
            int width = Math.abs(startPoint.x - endPoint.x);
            int height = Math.abs(startPoint.y - endPoint.y);
            // Draw a oval with the calculated properties
            g.drawOval(x, y, width, height);
            
        }

        @Override // Method to update the ending point of the OvalShape
        public void addPoint(Point point) {
            // Update the ending point to the provided point (used for resizing the oval)
            endPoint = point;
        }
        
    }
    

