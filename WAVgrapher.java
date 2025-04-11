/* 
Basic GUI Video
https://www.youtube.com/watch?v=5o3fMLPY7qY

Buttons
https://www.javatpoint.com/java-jbutton

Dialog Box for Loading file
https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html

File Filter
https://www.codejava.net/java-se/swing/add-file-filter-for-jfilechooser-dialog#:~:text=In%20Swing%2C%20we%20can%20do,f%20satisfies%20a%20filter%20condition.

https://www.tabnine.com/code/java/methods/javax.swing.JFileChooser/setFileFilter

Audiosystem documentation
https://docs.oracle.com/javase/8/docs/api/javax/sound/sampled/AudioSystem.html

Byte array from audio file
https://docs.oracle.com/javase/8/docs/technotes/guides/sound/programmer_guide/chapter7.html

Custom painting on panels
https://docs.oracle.com/javase/tutorial/uiswing/painting/step2.html

Split panes
https://docs.oracle.com/javase/tutorial/uiswing/components/splitpane.html
*/

package com.mycompany.wavgrapher;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.swing.filechooser.FileFilter;

public class WAVgrapher extends JFrame {
    private JPanel Channel1;
    private JPanel Channel2;
    private JSplitPane splitscreen;
    private File selectedFile;
    private JPanel bottomrow;
    private JLabel Sampleratetext;
    private JLabel Samplecount;
    private AudioInputStream AudioStream;
    private AudioFormat AudioFormat;
    private float Samplerate;
    private int samplecount;
    private short[] samples;
    
    public WAVgrapher() {
        setTitle("Wav File Grapher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        Channel1 = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                plotWav(g);
            }
        };
        Channel2 = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                plotWav2(g);
            }
        };
        splitscreen = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, Channel1, Channel2);
        Channel1.setBackground(Color.BLACK);
        Channel2.setBackground(Color.BLACK);
        JButton openButton = new JButton("Open .wav File");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".wav") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Wave Files (*.wav)";
                    }
                });

                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    loadWaveformData(selectedFile);
                    Channel1.repaint();
                    Channel2.repaint();
                    splitscreen.setDividerLocation(0.5);
                    Sampleratetext.setText("Samples Frequency: "+Samplerate);
                    Samplecount.setText("Total Samples: "+samplecount);
                }
            }
        });
        bottomrow = new JPanel();
        Samplecount = new JLabel("Total Samples: ");
        Sampleratetext = new JLabel("Samples Frequency: ");
        bottomrow.setLayout(new GridLayout(0,3));
        bottomrow.add(openButton);
        bottomrow.add(Sampleratetext);
        bottomrow.add(Samplecount);

        setLayout(new BorderLayout());
        add(splitscreen, BorderLayout.CENTER);
        splitscreen.setDividerLocation(0.5);
        add(bottomrow, BorderLayout.SOUTH);
    }

    private void loadWaveformData(File file) {
        try {
            AudioStream = AudioSystem.getAudioInputStream(file);
            AudioFormat = AudioStream.getFormat();
            Samplerate = AudioFormat.getSampleRate();
            samplecount = (int) (AudioFormat.getChannels() * AudioStream.getFrameLength());
            byte[] Audiodata = new byte[samplecount*2];
            AudioStream.read(Audiodata);
            samples = new short[Audiodata.length / 2];
            ByteBuffer.wrap(Audiodata).order(AudioFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            AudioStream.close();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    private void plotWav(Graphics g) {
        if (samples != null) {
            int width = Channel1.getWidth();
            int height = Channel1.getHeight();
            int yOffset = height / 2;
            int max = getMax(samples);
            int min = getMin(samples);
            g.setColor(Color.GREEN);

            for (int i = 0; i < (samples.length/2) - 1; i++) {
                int x1 = (int) i * width / (samples.length/2);
                int y1 = (int) yOffset + (samples[i*2] * height / (max-min));
                int x2 = (int) (i + 1) * width / (samples.length/2);
                int y2 = (int) yOffset + (samples[(i + 1)*2] * height / (max-min));
                g.drawLine(x1, y1, x2, y2);
            }
        }
        
    }
    
    private void plotWav2(Graphics g) {
        if (samples != null) {
           
            int max = getMax(samples);
            int min = getMin(samples);
            int width2 = Channel2.getWidth();
            int height2 = Channel2.getHeight();
            int yOffset2 = height2 / 2;
            g.setColor(Color.GREEN);
            for (int i = 0; i < (samples.length/2) - 1; i++) {
                int x3 = (int) i * width2 / (samples.length/2);
                int y3 = (int) yOffset2 + (samples[i*2 + 1] * height2 / (max-min));
                int x4 = (int) (i + 1) * width2 / (samples.length/2);
                int y4 = (int) yOffset2 + (samples[(i + 1)*2 +1] * height2 / (max-min));
                g.drawLine(x3, y3, x4, y4);
            }
        }
        
    }
    
    private short getMax(short[] data) {
        short max = Short.MIN_VALUE;
        short tmp;
        for(short i:data){
            tmp = i;
            if(max < tmp){
                max = tmp;
            }

        }
        return max;
    }
    
    private short getMin(short[] data) {
        short min = Short.MAX_VALUE;
        
        short tmp;
        for(short i:data){
            tmp = i;
            if(min > tmp){
                min = tmp;
            }

        }
        return min;
    }

    public static void main(String[] args) {
        WAVgrapher plotter = new WAVgrapher();
        plotter.setVisible(true);
    }
}
