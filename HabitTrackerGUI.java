import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;

public class HabitTrackerGUI {

    JFrame frame;
    JPanel gridPanel;

    JComboBox<String> monthBox;
    JComboBox<Integer> yearBox;

    ArrayList<String> habits = new ArrayList<>();
    HashMap<String, JCheckBox[]> habitBoxes = new HashMap<>();

    File habitListFile = new File("habitList.txt");

    int currentDays = 31;

    Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
    Font normalFont = new Font("Segoe UI", Font.PLAIN, 13);

    Color bgColor = new Color(245,248,252);
    Color headerColor = new Color(60,120,200);

    public HabitTrackerGUI(){

        frame = new JFrame("Habit Tracker");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(bgColor);

        createTopPanel();

        gridPanel = new JPanel();
        gridPanel.setBackground(bgColor);

        rebuildGrid();

        JScrollPane scroll = new JScrollPane(gridPanel);
        frame.add(scroll,BorderLayout.CENTER);

        createBottomPanel();

        loadHabitList();

        frame.setSize(1200,500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    void createTopPanel(){

        JPanel top = new JPanel();
        top.setBackground(headerColor);

        JLabel title = new JLabel("Habit Tracker");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI",Font.BOLD,20));

        String[] months = Arrays.stream(Month.values())
                .map(m -> m.name())
                .toArray(String[]::new);

        monthBox = new JComboBox<>(months);

        yearBox = new JComboBox<>();
        for(int i=2024;i<=2035;i++)
            yearBox.addItem(i);

        JButton loadBtn = new JButton("Load Month");

        loadBtn.addActionListener(e -> loadMonth());

        top.add(title);
        top.add(new JLabel("Month:"));
        top.add(monthBox);
        top.add(new JLabel("Year:"));
        top.add(yearBox);
        top.add(loadBtn);

        frame.add(top,BorderLayout.NORTH);
    }

    void createHeader(){

        JLabel habit = new JLabel("Habit");
        habit.setFont(headerFont);
        gridPanel.add(habit);

        for(int i=1;i<=currentDays;i++){

            JLabel day = new JLabel(String.valueOf(i),SwingConstants.CENTER);
            day.setFont(headerFont);
            gridPanel.add(day);
        }
    }

    void createBottomPanel(){

        JPanel bottom = new JPanel();

        JButton addBtn = new JButton("Add Habit");
        JButton deleteBtn = new JButton("Delete Habit");
        JButton saveBtn = new JButton("Save Progress");

        addBtn.addActionListener(e -> addHabit());
        deleteBtn.addActionListener(e -> deleteHabit());
        saveBtn.addActionListener(e -> saveMonth());

        bottom.add(addBtn);
        bottom.add(deleteBtn);
        bottom.add(saveBtn);

        frame.add(bottom,BorderLayout.SOUTH);
    }

    void addHabit(){

        String habit = JOptionPane.showInputDialog(frame,"Enter Habit Name:");

        if(habit == null || habit.trim().equals(""))
            return;

        habits.add(habit);

        JCheckBox[] boxes = new JCheckBox[31];

        for(int i=0;i<31;i++){
            boxes[i] = new JCheckBox();
            boxes[i].setBackground(bgColor);
        }

        habitBoxes.put(habit,boxes);

        saveHabitList();
        rebuildGrid();
    }

    void deleteHabit(){

        String habit = JOptionPane.showInputDialog(frame,"Enter Habit Name to Delete:");

        if(habit == null)
            return;

        if(!habits.contains(habit)){
            JOptionPane.showMessageDialog(frame,"Habit Not Found");
            return;
        }

        habits.remove(habit);
        habitBoxes.remove(habit);

        saveHabitList();
        rebuildGrid();
    }

    void rebuildGrid(){

        gridPanel.removeAll();

        gridPanel.setLayout(new GridLayout(0,currentDays+1,4,4));

        createHeader();

        for(String habit : habits){

            JLabel label = new JLabel(habit);
            label.setFont(normalFont);
            gridPanel.add(label);

            JCheckBox[] boxes = habitBoxes.get(habit);

            if(boxes == null){

                boxes = new JCheckBox[31];

                for(int i=0;i<31;i++){
                    boxes[i] = new JCheckBox();
                    boxes[i].setBackground(bgColor);
                }

                habitBoxes.put(habit,boxes);
            }

            for(int i=0;i<currentDays;i++)
                gridPanel.add(boxes[i]);
        }

        refreshUI();
    }

    void refreshUI(){
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    void saveHabitList(){

        try{

            PrintWriter writer = new PrintWriter(habitListFile);

            for(String habit : habits)
                writer.println(habit);

            writer.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void loadHabitList(){

        try{

            if(!habitListFile.exists())
                return;

            Scanner sc = new Scanner(habitListFile);

            while(sc.hasNextLine()){

                String habit = sc.nextLine();
                habits.add(habit);

                JCheckBox[] boxes = new JCheckBox[31];

                for(int i=0;i<31;i++){
                    boxes[i] = new JCheckBox();
                    boxes[i].setBackground(bgColor);
                }

                habitBoxes.put(habit,boxes);
            }

            sc.close();

            rebuildGrid();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void saveMonth(){

        try{

            String month = monthBox.getSelectedItem().toString();
            int year = (int)yearBox.getSelectedItem();

            File file = new File(month+"_"+year+".txt");

            PrintWriter writer = new PrintWriter(file);

            for(String habit : habits){

                writer.print(habit);

                JCheckBox[] boxes = habitBoxes.get(habit);

                for(int i=0;i<currentDays;i++){

                    if(boxes[i].isSelected())
                        writer.print(",1");
                    else
                        writer.print(",0");
                }

                writer.println();
            }

            writer.close();

            JOptionPane.showMessageDialog(frame,"Month Saved");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void loadMonth(){

        try{

            int year = (int)yearBox.getSelectedItem();
            int monthIndex = monthBox.getSelectedIndex()+1;

            currentDays = YearMonth.of(year,monthIndex).lengthOfMonth();

            String month = monthBox.getSelectedItem().toString();

            File file = new File(month+"_"+year+".txt");

            rebuildGrid();

            for(String habit : habits){
                JCheckBox[] boxes = habitBoxes.get(habit);

                for(int i=0;i<31;i++)
                    boxes[i].setSelected(false);
            }

            if(!file.exists()){
                JOptionPane.showMessageDialog(frame,"New Month Started");
                refreshUI();
                return;
            }

            Scanner sc = new Scanner(file);

            while(sc.hasNextLine()){

                String line = sc.nextLine();
                String[] parts = line.split(",");

                String habit = parts[0];
                JCheckBox[] boxes = habitBoxes.get(habit);

                for(int i=1;i<parts.length;i++){

                    if(parts[i].equals("1"))
                        boxes[i-1].setSelected(true);
                }
            }

            sc.close();
            refreshUI();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){

        SwingUtilities.invokeLater(() -> new HabitTrackerGUI());

    }
}