# Swing 界面示例源码

来源：Swing 界面示例转换资料。

## BorderLayoutDemo.java

```java
package cn.edu.zucc.booklib.LayoutDemo;

import javax.swing.*;
import java.awt.*;

public class BorderLayoutDemo extends JFrame {
	public BorderLayoutDemo() { // 构造函数，初始化对象值
		// 设置为边界布局，组件间横向、纵向间距均为5像素
		setLayout(new BorderLayout(5, 5));
		setFont(new Font("Helvetica", Font.PLAIN, 14));
		getContentPane().add("North", new JButton("North")); // 将按钮添加到窗口中
		getContentPane().add("South", new JButton("South"));
		getContentPane().add("East", new JButton("East"));
		getContentPane().add("West", new JButton("West"));
		getContentPane().add("Center", new JButton("Center"));
	}

	public static void main(String args[]) {
		BorderLayoutDemo f = new BorderLayoutDemo();
		f.setTitle("边界布局");
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null); // 让窗体居中显示
	}
}
```

## BorderLayoutDemo1.java

```java
package cn.edu.zucc.booklib.LayoutDemo;

import javax.swing.*;
import java.awt.*;

public class BorderLayoutDemo1 extends JFrame {
	JPanel p = new JPanel();

	public BorderLayoutDemo1() {
		setLayout(new BorderLayout(5, 5));
		setFont(new Font("Helvetica", Font.PLAIN, 14));
		getContentPane().add("North", new JButton("North"));
		getContentPane().add("South", new JButton("South"));
		getContentPane().add("East", new JButton("East"));
		getContentPane().add("West", new JButton("West"));
		// 设置面板为流式布局居中显示，组件横、纵间距为5个像素
		p.setLayout(new FlowLayout(1, 5, 5));
		// 使用循环添加按钮，注意每次添加的按钮对象名称都是b
		// 但按钮每次均是用new新生成的，所有代表不同的按钮对象。
		for (int i = 1; i < 10; i++) {
			// String.valueOf(i)，将数字转换为字符串
			JButton b = new JButton(String.valueOf(i));
			p.add(b); // 将按钮添加到面板中
		}
		getContentPane().add("Center", p); // 将面板添加到中间位置
	}

	public static void main(String args[]) {
		BorderLayoutDemo1 f = new BorderLayoutDemo1();
		f.setTitle("边界布局");
		f.pack(); // 让窗体自适应组建大小
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null); // 让窗体居中显示
	}
}

```

## FlowLayoutDemo.java

```java
package cn.edu.zucc.booklib.LayoutDemo;

import javax.swing.*;
import java.awt.*;

public class FlowLayoutDemo extends JFrame {
	public FlowLayoutDemo() {
		// 设置窗体为流式布局，无参数默认为居中对齐
		setLayout(new FlowLayout());
		// 设置窗体中显示的字体样式
		setFont(new Font("Helvetica", Font.PLAIN, 14));
		// 将按钮添加到窗体中
		getContentPane().add(new JButton("Button 1"));
		getContentPane().add(new JButton("Button 2"));
		getContentPane().add(new JButton("Button 3"));
		getContentPane().add(new JButton("Button 4"));
	}

	public static void main(String args[]) {
		FlowLayoutDemo window = new FlowLayoutDemo();
		window.setTitle("流式布局");
		// 该代码依据放置的组件设定窗口的大小使之正好能容纳你放置的所有组件
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null); // 让窗体居中显示
	}
}

```

## GridFrame.java

```java
package cn.edu.zucc.booklib.LayoutDemo;

import java.awt.*;
import javax.swing.*;

class GridFrame extends JFrame {
	// 定义面板，并设置为网格布局，4行4列，组件水平、垂直间距均为3
	JPanel p = new JPanel(new GridLayout(4, 4, 3, 3));
	JTextArea t = new JTextArea(); // 定义文本框
	// 定义字符串数组，为按钮的显示文本赋值
	// 注意字符元素的顺序与循环添加按钮保持一致
	String str[] = { "7", "8", "9", "/", "4", "5", "6", "*", "1", "2", "3",
			"-", "0", ".", "=", "+" };

	public GridFrame(String s) {
		super(s); // 为窗体名称赋值
		setLayout(new BorderLayout()); // 定义窗体布局为边界布局
		JButton btn[]; // 声明按钮数组
		btn = new JButton[str.length]; // 创建按钮数组
		// 循环定义按钮，并添加到面板中
		for (int i = 0; i < str.length; i++) {
			btn[i] = new JButton(str[i]);
			p.add(btn[i]);
		}
		// 将文本框放置在窗体NORTH位置
		getContentPane().add(t, BorderLayout.NORTH);
		// 将面板放置在窗体CENTER位置
		getContentPane().add(p, BorderLayout.CENTER);
		setVisible(true);
		setSize(250, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // 让窗体居中显示
	}

	public static void main(String[] args) {
		GridFrame gl = new GridFrame("网格布局计算机！");
	}
}

```

## GridLayoutDemo.java

```java
package cn.edu.zucc.booklib.LayoutDemo;

import javax.swing.*;

import java.awt.*;

public class GridLayoutDemo extends JFrame {

	public GridLayoutDemo() {
		setLayout(new GridLayout(0, 2)); // 设置为网格布局，未指定行数
		setFont(new Font("Helvetica", Font.PLAIN, 14));
		getContentPane().add(new JButton("Button 1"));
		getContentPane().add(new JButton("Button 2"));
		getContentPane().add(new JButton("Button 3"));
		getContentPane().add(new JButton("Button 4"));
		getContentPane().add(new JButton("Button 5"));

	}

	public static void main(String args[]) {

		GridLayoutDemo f = new GridLayoutDemo();
		f.setTitle("GridWindow Application");
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null); // 让窗体居中显示

	}

}

```

## NullLayoutDemo.java

```java
package cn.edu.zucc.booklib.LayoutDemo;

import java.awt.*;

import javax.swing.*;

public class NullLayoutDemo {
	JFrame fr;
	JButton a, b;
	NullLayoutDemo() {
		fr = new JFrame();
		fr.setBounds(100, 100, 250, 150);

		// 设置窗体为空布局
		fr.setLayout(null);
		a = new JButton("按钮a");
		b = new JButton("按钮b");
		fr.getContentPane().add(a);

		// 设置按钮a的精确位置
		a.setBounds(30, 30, 80, 25);
		fr.getContentPane().add(b);
		b.setBounds(150, 40, 80, 25);
		fr.setTitle("NullLayoutDemo");
		fr.setVisible(true);
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setLocationRelativeTo(null); // 让窗体居中显示
	}

	public static void main(String args[]) {
		new NullLayoutDemo();
	}

}
```

## cardlayout.java

```java
package cn.edu.zucc.booklib.LayoutDemo;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;//引入事件包

//定义类时实现监听接口

public class cardlayout extends JFrame implements ActionListener {
	JButton nextbutton;
	JButton preButton;
	Panel cardPanel = new Panel();
	Panel controlpaPanel = new Panel();

	// 定义卡片布局对象
	CardLayout card = new CardLayout();

	// 定义构造函数
	public cardlayout() {
		super();
		setSize(300, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);

		// 设置cardPanel面板对象为卡片布局
		cardPanel.setLayout(card);

		// 循环，在cardPanel面板对象中添加五个按钮
		// 因为cardPanel面板对象为卡片布局，因此只显示最先添加的组件
		for (int i = 0; i < 5; i++) {
			cardPanel.add(new JButton("按钮" + i),"one");
		}

		// 实例化按钮对象
		nextbutton = new JButton("下一张卡片");
		preButton = new JButton("上一张卡片");

		// 为按钮对象注册监听器
		nextbutton.addActionListener(this);
		preButton.addActionListener(this);
		controlpaPanel.add(preButton);
		controlpaPanel.add(nextbutton);

		// 定义容器对象为当前窗体容器对象
		Container container = getContentPane();

		// 将 cardPanel面板放置在窗口边界布局的中间，窗口默认为边界布局
		container.add(cardPanel, BorderLayout.CENTER);

		// 将controlpaPanel面板放置在窗口边界布局的南边，
		container.add(controlpaPanel, BorderLayout.SOUTH);
	}

	// 实现按钮的监听触发时的处理
	public void actionPerformed(ActionEvent e) {
		// 如果用户单击nextbutton，执行的语句
		if (e.getSource() == nextbutton) {
			// 切换cardPanel面板中当前组件之后的一个组件
			// 若当前组件为最后添加的组件，则显示第一个组件，即卡片组件显示是循环的。
			card.next(cardPanel);
		}

		if (e.getSource() == preButton) {
			// 切换cardPanel面板中当前组件之前的一个组件
			// 若当前组件为第一个添加的组件，则显示最后一个组件，即卡片组件显示是循环的。
			card.previous(cardPanel);
		}
	}

	public static void main(String[] args) {
		cardlayout kapian = new cardlayout();
	}

}

```
