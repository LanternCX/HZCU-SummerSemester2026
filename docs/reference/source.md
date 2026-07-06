# 个人计划管理案例源码

来源：个人计划管理案例源码转换资料。

## PersonPlanStarter.java

```java
package cn.edu.zucc.personplan;

import cn.edu.zucc.personplan.ui.FrmMain;

public class PersonPlanStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new FrmMain();
	}

}

```

## PersonPlanUtil.java

```java
package cn.edu.zucc.personplan;

import cn.edu.zucc.personplan.comtrol.example.ExamplePlanManager;
import cn.edu.zucc.personplan.comtrol.example.ExampleStepManager;
import cn.edu.zucc.personplan.comtrol.example.ExampleUserManager;
import cn.edu.zucc.personplan.itf.IPlanManager;
import cn.edu.zucc.personplan.itf.IStepManager;
import cn.edu.zucc.personplan.itf.IUserManager;

public class PersonPlanUtil {
	public static IPlanManager planManager=new ExamplePlanManager();//需要换成自行设计的实现类
	public static IStepManager stepManager=new ExampleStepManager();//需要换成自行设计的实现类
	public static IUserManager userManager=new ExampleUserManager();//需要换成自行设计的实现类
	
}

```

## ExamplePlanManager.java

```java
package cn.edu.zucc.personplan.comtrol.example;

import java.util.ArrayList;
import java.util.List;

import cn.edu.zucc.personplan.itf.IPlanManager;
import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.util.BaseException;

public class ExamplePlanManager implements IPlanManager {

	@Override
	public BeanPlan addPlan(String name) throws BaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BeanPlan> loadAll() throws BaseException {
		List<BeanPlan> result=new ArrayList<BeanPlan>();
		BeanPlan p=new BeanPlan();
		result.add(p);
		return result;
	}

	@Override
	public void deletePlan(BeanPlan plan) throws BaseException {
		
	}

}

```

## ExampleStepManager.java

```java
package cn.edu.zucc.personplan.comtrol.example;

import java.util.ArrayList;
import java.util.List;

import cn.edu.zucc.personplan.itf.IStepManager;
import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.model.BeanStep;
import cn.edu.zucc.personplan.util.BaseException;

public class ExampleStepManager implements IStepManager {

	@Override
	public void add(BeanPlan plan, String name, String planstartdate,
			String planfinishdate) throws BaseException {
		
		
	}

	@Override
	public List<BeanStep> loadSteps(BeanPlan plan) throws BaseException {
		List<BeanStep> result=new ArrayList<BeanStep>();
		BeanStep p=new BeanStep();
		result.add(p);
		return result;
	}

	@Override
	public void deleteStep(BeanStep step) throws BaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startStep(BeanStep step) throws BaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishStep(BeanStep step) throws BaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveUp(BeanStep step) throws BaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveDown(BeanStep step) throws BaseException {
		// TODO Auto-generated method stub
		
	}

}

```

## ExampleUserManager.java

```java
package cn.edu.zucc.personplan.comtrol.example;

import cn.edu.zucc.personplan.itf.IUserManager;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;

public class ExampleUserManager implements IUserManager {

	@Override
	public BeanUser reg(String userid, String pwd,String pwd2) throws BaseException {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public BeanUser login(String userid, String pwd) throws BaseException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void changePwd(BeanUser user, String oldPwd, String newPwd,
			String newPwd2) throws BaseException {
		// TODO Auto-generated method stub
		
	}

}

```

## IPlanManager.java

```java
package cn.edu.zucc.personplan.itf;

import java.util.List;

import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.util.BaseException;

public interface IPlanManager {
	/**
	 * 添加计划
	 * 要求新增的计划的排序号为当前用户现有最大排序号+1
	 * 注意：当前登陆用户可通过 BeanUser.currentLoginUser获取
	 * @param name  计划名称
	 * @throws BaseException
	 */
	public BeanPlan addPlan(String name) throws BaseException;
	/**
	 * 提取当前用户所有计划
	 * @return
	 * @throws BaseException
	 */
	public List<BeanPlan> loadAll()throws BaseException;
	/**
	 * 删除计划，如果计划下存在步骤，则不允许删除
	 * @param plan
	 * @throws BaseException
	 */
	public void deletePlan(BeanPlan plan)throws BaseException;

}

```

## IStepManager.java

```java
package cn.edu.zucc.personplan.itf;

import java.util.List;

import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.model.BeanStep;
import cn.edu.zucc.personplan.util.BaseException;

public interface IStepManager {
	public List<BeanStep> loadSteps(BeanPlan plan)throws BaseException;
	
	/**
	 * 添加步骤
	 * 新填的步骤序号为当前计划最大步骤序号+1
	 * 注意：需完成字符串和时间类型的转换，添加后需调整计划表中相应的数量值
	 * @param plan
	 * @param name
	 * @param planstartdate
	 * @param planfinishdate
	 * @throws BaseException
	 */
	public void add(BeanPlan plan, String name, String planstartdate, String planfinishdate)throws BaseException;

	
	
	/**
	 * 删除步骤，
	 * 注意删除后需调整计划表中对应的步骤数量
	 * @param step
	 * @throws BaseException
	 */
	public void deleteStep(BeanStep step)throws BaseException;

	/**
	 * 设置当前步骤的实际开始时间，及对应的计划表中已开始步骤数量
	 * 
	 * @param step
	 * @throws BaseException
	 */
	public void startStep(BeanStep step)throws BaseException;
	/**
	 * 设置当前步骤的实际完成时间，及对应的计划表中已完成步骤数量
	 * @param step
	 * @throws BaseException
	 */
	public void finishStep(BeanStep step)throws BaseException;
	/**
	 * 调整当前步骤的顺序号
	 * 注意：数据库表中，plan_id,step_order上建立了唯一索引，调整当前步骤的序号值和上一步骤的序号值时不能出现序号值一样的情况
	 * @param step
	 * @throws BaseException
	 */
	public void moveUp(BeanStep step)throws BaseException;
	public void moveDown(BeanStep step)throws BaseException;
	

}

```

## IUserManager.java

```java
package cn.edu.zucc.personplan.itf;

import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;

public interface IUserManager {
	/**
	 * 注册：
	 * 要求用户名不能重复，不能为空
	 * 两次输入的密码必须一致，密码不能为空
	 * 如果注册失败，则抛出异常
	 * @param userid
	 * @param pwd  密码
	 * @param pwd2 重复输入的密码
	 * @return
	 * @throws BaseException
	 */
	public BeanUser reg(String userid, String pwd,String pwd2) throws BaseException;
	/**
	 * 登陆
	 * 1、如果用户不存在或者密码错误，抛出一个异常
	 * 2、如果认证成功，则返回当前用户信息
	 * @param userid
	 * @param pwd
	 * @return
	 * @throws BaseException
	 */
	public BeanUser login(String userid,String pwd)throws BaseException;
	/**
	 * 修改密码
	 * 如果没有成功修改，则抛出异常
	 * @param user    当前用户
	 * @param oldPwd  原密码
	 * @param newPwd  新密码
	 * @param newPwd2 重复输入的新密码
	 */
	public void changePwd(BeanUser user, String oldPwd,String newPwd, String newPwd2)throws BaseException;
}

```

## BeanPlan.java

```java
package cn.edu.zucc.personplan.model;

public class BeanPlan {
	public static final String[] tableTitles={"序号","名称","步骤数","已完成数"};
	/**
	 * 请自行根据javabean的设计修改本函数代码，col表示界面表格中的列序号，0开始
	 */
	public String getCell(int col){
		if(col==0) return "1";
		else if(col==1) return "示例计划";
		else if(col==2) return "2";
		else if(col==3) return "1";
		else return "";
	}

}

```

## BeanStep.java

```java
package cn.edu.zucc.personplan.model;

public class BeanStep {
	public static final String[] tblStepTitle={"序号","名称","计划开始时间","计划完成时间","实际开始时间","实际完成时间"};
	/**
	 * 请自行根据javabean的设计修改本函数代码，col表示界面表格中的列序号，0开始
	 */
	public String getCell(int col){
		if(col==0) return "1";
		else if(col==1) return "示例步骤";
		else if(col==2) return "2015-01-01";
		else if(col==3) return "2015-08-01";
		else if(col==4) return "";
		else if(col==5) return "";
		
		else return "";
	}
}

```

## BeanUser.java

```java
package cn.edu.zucc.personplan.model;

public class BeanUser {
	public static BeanUser currentLoginUser=null;

}

```

## FrmAddPlan.java

```java
package cn.edu.zucc.personplan.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cn.edu.zucc.personplan.PersonPlanUtil;
import cn.edu.zucc.personplan.util.BaseException;


public class FrmAddPlan extends JDialog implements ActionListener {
	
	private JPanel toolBar = new JPanel();
	private JPanel workPane = new JPanel();
	private Button btnOk = new Button("确定");
	private Button btnCancel = new Button("取消");
	private JLabel labelName = new JLabel("名称：");
	
	private JTextField edtName = new JTextField(20);
	public FrmAddPlan(JFrame f, String s, boolean b) {
		super(f, s, b);
		toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		toolBar.add(btnOk);
		toolBar.add(btnCancel);
		this.getContentPane().add(toolBar, BorderLayout.SOUTH);
		workPane.add(labelName);
		workPane.add(edtName);
		this.getContentPane().add(workPane, BorderLayout.CENTER);
		this.setSize(320, 180);
		// 屏幕居中显示
		double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		this.setLocation((int) (width - this.getWidth()) / 2,
				(int) (height - this.getHeight()) / 2);

		this.validate();
		this.btnOk.addActionListener(this);
		this.btnCancel.addActionListener(this);
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==this.btnCancel) {
			this.setVisible(false);
			return;
		}
		else if(e.getSource()==this.btnOk){
			String name=this.edtName.getText();
			try {
				PersonPlanUtil.planManager.addPlan(name);
				this.setVisible(false);
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
	}
	
	
}

```

## FrmAddStep.java

```java
package cn.edu.zucc.personplan.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cn.edu.zucc.personplan.PersonPlanUtil;
import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.util.BaseException;


public class FrmAddStep extends JDialog implements ActionListener {
	public BeanPlan plan=null;
	private JPanel toolBar = new JPanel();
	private JPanel workPane = new JPanel();
	private Button btnOk = new Button("确定");
	private Button btnCancel = new Button("取消");
	private JLabel labelName = new JLabel("计划步骤名称：");
	private JLabel labelPlanStartDate = new JLabel("计划开始日期：");
	private JLabel labelPlanFinishDate = new JLabel("计划完成日期：");
	
	private JTextField edtPlanStartDate = new JTextField(20);
	private JTextField edtPlanFinishDate = new JTextField(20);
	private JTextField edtName = new JTextField(20);
	public FrmAddStep(JFrame f, String s, boolean b) {
		super(f, s, b);
		toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		toolBar.add(btnOk);
		toolBar.add(btnCancel);
		this.getContentPane().add(toolBar, BorderLayout.SOUTH);
		workPane.add(labelName);
		workPane.add(edtName);
		workPane.add(labelPlanStartDate);
		workPane.add(edtPlanStartDate);
		workPane.add(labelPlanFinishDate);
		workPane.add(edtPlanFinishDate);
		this.getContentPane().add(workPane, BorderLayout.CENTER);
		this.setSize(380, 180);
		// 屏幕居中显示
		double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		this.setLocation((int) (width - this.getWidth()) / 2,
				(int) (height - this.getHeight()) / 2);

		this.validate();
		this.btnOk.addActionListener(this);
		this.btnCancel.addActionListener(this);
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==this.btnCancel) {
			this.setVisible(false);
			return;
		}
		else if(e.getSource()==this.btnOk){
			String name=this.edtName.getText();
			String sd=this.edtPlanStartDate.getText();
			String fd=this.edtPlanFinishDate.getText();
			try {
				PersonPlanUtil.stepManager.add(plan,name,sd,fd);
				this.setVisible(false);
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		
		}
		
	}
	
	
}

```

## FrmLogin.java

```java
package cn.edu.zucc.personplan.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cn.edu.zucc.personplan.PersonPlanUtil;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;


public class FrmLogin extends JDialog implements ActionListener {
	private JPanel toolBar = new JPanel();
	private JPanel workPane = new JPanel();
	private JButton btnLogin = new JButton("登陆");
	private JButton btnCancel = new JButton("退出");
	private JButton btnRegister = new JButton("注册");
	
	private JLabel labelUser = new JLabel("用户：");
	private JLabel labelPwd = new JLabel("密码：");
	private JTextField edtUserId = new JTextField(20);
	private JPasswordField edtPwd = new JPasswordField(20);

	public FrmLogin(Frame f, String s, boolean b) {
		super(f, s, b);
		toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		toolBar.add(this.btnRegister);
		toolBar.add(btnLogin);
		toolBar.add(btnCancel);
		this.getContentPane().add(toolBar, BorderLayout.SOUTH);
		workPane.add(labelUser);
		workPane.add(edtUserId);
		workPane.add(labelPwd);
		workPane.add(edtPwd);
		this.getContentPane().add(workPane, BorderLayout.CENTER);
		this.setSize(320, 140);
		// 屏幕居中显示
		double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		this.setLocation((int) (width - this.getWidth()) / 2,
				(int) (height - this.getHeight()) / 2);

		this.validate();

		btnLogin.addActionListener(this);
		btnCancel.addActionListener(this);
		this.btnRegister.addActionListener(this);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btnLogin) {
			String userid=this.edtUserId.getText();
			String pwd=new String(this.edtPwd.getPassword());
			try {
				BeanUser.currentLoginUser= PersonPlanUtil.userManager.login(userid, pwd);
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
			this.setVisible(false);
			
		} else if (e.getSource() == this.btnCancel) {
			System.exit(0);
		} else if(e.getSource()==this.btnRegister){
			FrmRegister dlg=new FrmRegister(this,"注册",true);
			dlg.setVisible(true);
		}
	}

}

```

## FrmMain.java

```java
package cn.edu.zucc.personplan.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import cn.edu.zucc.personplan.PersonPlanUtil;
import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.model.BeanStep;
import cn.edu.zucc.personplan.util.BaseException;



public class FrmMain extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JMenuBar menubar=new JMenuBar(); ;
    private JMenu menu_plan=new JMenu("计划管理");
    private JMenu menu_step=new JMenu("步骤管理");
    private JMenu menu_static=new JMenu("查询统计");
    private JMenu menu_more=new JMenu("更多");
    
    private JMenuItem  menuItem_AddPlan=new JMenuItem("新建计划");
    private JMenuItem  menuItem_DeletePlan=new JMenuItem("删除计划");
    private JMenuItem  menuItem_AddStep=new JMenuItem("添加步骤");
    private JMenuItem  menuItem_DeleteStep=new JMenuItem("删除步骤");
    private JMenuItem  menuItem_startStep=new JMenuItem("开始步骤");
    private JMenuItem  menuItem_finishStep=new JMenuItem("结束步骤");
    private JMenuItem  menuItem_moveUpStep=new JMenuItem("步骤上移");
    private JMenuItem  menuItem_moveDownStep=new JMenuItem("步骤下移");
    
    private JMenuItem  menuItem_modifyPwd=new JMenuItem("密码修改");
    
    private JMenuItem  menuItem_static1=new JMenuItem("统计1");
    
    
	private FrmLogin dlgLogin=null;
	private JPanel statusBar = new JPanel();
	
	private Object tblPlanTitle[]=BeanPlan.tableTitles;
	private Object tblPlanData[][];
	DefaultTableModel tabPlanModel=new DefaultTableModel();
	private JTable dataTablePlan=new JTable(tabPlanModel);
	
	
	private Object tblStepTitle[]=BeanStep.tblStepTitle;
	private Object tblStepData[][];
	DefaultTableModel tabStepModel=new DefaultTableModel();
	private JTable dataTableStep=new JTable(tabStepModel);
	
	private BeanPlan curPlan=null;
	List<BeanPlan> allPlan=null;
	List<BeanStep> planSteps=null;
	private void reloadPlanTable(){//这是测试数据，需要用实际数替换
		try {
			allPlan=PersonPlanUtil.planManager.loadAll();
		} catch (BaseException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
			return;
		}
		tblPlanData =  new Object[allPlan.size()][BeanPlan.tableTitles.length];
		for(int i=0;i<allPlan.size();i++){
			for(int j=0;j<BeanPlan.tableTitles.length;j++)
				tblPlanData[i][j]=allPlan.get(i).getCell(j);
		}
		tabPlanModel.setDataVector(tblPlanData,tblPlanTitle);
		this.dataTablePlan.validate();
		this.dataTablePlan.repaint();
	}
	private void reloadPlanStepTabel(int planIdx){
		if(planIdx<0) return;
		curPlan=allPlan.get(planIdx);
		try {
			planSteps=PersonPlanUtil.stepManager.loadSteps(curPlan);
		} catch (BaseException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
			return;
		}
		tblStepData =new Object[planSteps.size()][BeanStep.tblStepTitle.length];
		for(int i=0;i<planSteps.size();i++){
			for(int j=0;j<BeanStep.tblStepTitle.length;j++)
				tblStepData[i][j]=planSteps.get(i).getCell(j);
		}
		
		tabStepModel.setDataVector(tblStepData,tblStepTitle);
		this.dataTableStep.validate();
		this.dataTableStep.repaint();
	}
	public FrmMain(){
		
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setTitle("个人计划管理系统");
		dlgLogin=new FrmLogin(this,"登陆",true);
		dlgLogin.setVisible(true);
	    //菜单
	    this.menu_plan.add(this.menuItem_AddPlan); this.menuItem_AddPlan.addActionListener(this);
	    this.menu_plan.add(this.menuItem_DeletePlan); this.menuItem_DeletePlan.addActionListener(this);
	    this.menu_step.add(this.menuItem_AddStep); this.menuItem_AddStep.addActionListener(this);
	    this.menu_step.add(this.menuItem_DeleteStep); this.menuItem_DeleteStep.addActionListener(this);
	    this.menu_step.add(this.menuItem_startStep); this.menuItem_startStep.addActionListener(this);
	    this.menu_step.add(this.menuItem_finishStep); this.menuItem_finishStep.addActionListener(this);
	    this.menu_step.add(this.menuItem_moveUpStep); this.menuItem_moveUpStep.addActionListener(this);
	    this.menu_step.add(this.menuItem_moveDownStep); this.menuItem_moveDownStep.addActionListener(this);
	    this.menu_static.add(this.menuItem_static1); this.menuItem_static1.addActionListener(this);
	    this.menu_more.add(this.menuItem_modifyPwd); this.menuItem_modifyPwd.addActionListener(this);
	    
	    menubar.add(menu_plan);
	    menubar.add(menu_step);
	    menubar.add(menu_static);
	    menubar.add(menu_more);
	    this.setJMenuBar(menubar);
	    
	    this.getContentPane().add(new JScrollPane(this.dataTablePlan), BorderLayout.WEST);
	    this.dataTablePlan.addMouseListener(new MouseAdapter (){

			@Override
			public void mouseClicked(MouseEvent e) {
				int i=FrmMain.this.dataTablePlan.getSelectedRow();
				if(i<0) {
					return;
				}
				FrmMain.this.reloadPlanStepTabel(i);
			}
	    	
	    });
	    this.getContentPane().add(new JScrollPane(this.dataTableStep), BorderLayout.CENTER);
	    
	    this.reloadPlanTable();
	    //状态栏
	    statusBar.setLayout(new FlowLayout(FlowLayout.LEFT));
	    JLabel label=new JLabel("您好!");//修改成   您好！+登陆用户名
	    statusBar.add(label);
	    this.getContentPane().add(statusBar,BorderLayout.SOUTH);
	    this.addWindowListener(new WindowAdapter(){   
	    	public void windowClosing(WindowEvent e){ 
	    		System.exit(0);
             }
        });
	    this.setVisible(true);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==this.menuItem_AddPlan){
			FrmAddPlan dlg=new FrmAddPlan(this,"添加计划",true);
			dlg.setVisible(true);
		}
		else if(e.getSource()==this.menuItem_DeletePlan){
			if(this.curPlan==null) {
				JOptionPane.showMessageDialog(null, "请选择计划", "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				PersonPlanUtil.planManager.deletePlan(this.curPlan);
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else if(e.getSource()==this.menuItem_AddStep){
			FrmAddStep dlg=new FrmAddStep(this,"添加步骤",true);
			dlg.plan=curPlan;
			dlg.setVisible(true);
		}
		else if(e.getSource()==this.menuItem_DeleteStep){
			int i=FrmMain.this.dataTableStep.getSelectedRow();
			if(i<0) {
				JOptionPane.showMessageDialog(null, "请选择步骤", "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				PersonPlanUtil.stepManager.deleteStep(this.planSteps.get(i));
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else if(e.getSource()==this.menuItem_startStep){
			int i=FrmMain.this.dataTableStep.getSelectedRow();
			if(i<0) {
				JOptionPane.showMessageDialog(null, "请选择步骤", "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				PersonPlanUtil.stepManager.startStep(this.planSteps.get(i));
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else if(e.getSource()==this.menuItem_finishStep){
			int i=FrmMain.this.dataTableStep.getSelectedRow();
			if(i<0) {
				JOptionPane.showMessageDialog(null, "请选择步骤", "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				PersonPlanUtil.stepManager.finishStep(this.planSteps.get(i));
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else if(e.getSource()==this.menuItem_moveUpStep){
			int i=FrmMain.this.dataTableStep.getSelectedRow();
			if(i<0) {
				JOptionPane.showMessageDialog(null, "请选择步骤", "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				PersonPlanUtil.stepManager.moveUp(this.planSteps.get(i));
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else if(e.getSource()==this.menuItem_moveDownStep){
			int i=FrmMain.this.dataTableStep.getSelectedRow();
			if(i<0) {
				JOptionPane.showMessageDialog(null, "请选择步骤", "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				PersonPlanUtil.stepManager.moveDown(this.planSteps.get(i));
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else if(e.getSource()==this.menuItem_static1){
			
		}
		else if(e.getSource()==this.menuItem_modifyPwd){
			FrmModifyPwd dlg=new FrmModifyPwd(this,"密码修改",true);
			dlg.setVisible(true);
		}
	}
}

```

## FrmModifyPwd.java

```java
package cn.edu.zucc.personplan.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cn.edu.zucc.personplan.PersonPlanUtil;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;

public class FrmModifyPwd extends JDialog implements ActionListener {
	private JPanel toolBar = new JPanel();
	private JPanel workPane = new JPanel();
	private Button btnOk = new Button("确定");
	private Button btnCancel = new Button("取消");
	
	private JLabel labelPwdOld = new JLabel("原密码：");
	private JLabel labelPwd = new JLabel("新密码：");
	private JLabel labelPwd2 = new JLabel("新密码：");
	private JPasswordField edtPwdOld = new JPasswordField(20);
	private JPasswordField edtPwd = new JPasswordField(20);
	private JPasswordField edtPwd2 = new JPasswordField(20);
	public FrmModifyPwd(Frame f, String s, boolean b) {
		super(f, s, b);
		toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		toolBar.add(this.btnOk);
		toolBar.add(btnCancel);
		this.getContentPane().add(toolBar, BorderLayout.SOUTH);
		workPane.add(labelPwdOld);
		workPane.add(edtPwdOld);
		workPane.add(labelPwd);
		workPane.add(edtPwd);
		workPane.add(labelPwd2);
		workPane.add(edtPwd2);
		this.getContentPane().add(workPane, BorderLayout.CENTER);
		this.setSize(300, 180);
		this.btnCancel.addActionListener(this);
		this.btnOk.addActionListener(this);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==this.btnCancel)
			this.setVisible(false);
		else if(e.getSource()==this.btnOk){
			try {
				PersonPlanUtil.userManager.changePwd(BeanUser.currentLoginUser,new String(edtPwdOld.getPassword()),new String(edtPwd.getPassword()),new String(edtPwd2.getPassword()));
				this.setVisible(false);
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
			
		
	}


}

```

## FrmRegister.java

```java
package cn.edu.zucc.personplan.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cn.edu.zucc.personplan.PersonPlanUtil;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;

public class FrmRegister extends JDialog implements ActionListener {
	private JPanel toolBar = new JPanel();
	private JPanel workPane = new JPanel();
	private Button btnOk = new Button("注册");
	private Button btnCancel = new Button("取消");
	
	private JLabel labelUser = new JLabel("用户：");
	private JLabel labelPwd = new JLabel("密码：");
	private JLabel labelPwd2 = new JLabel("密码：");
	private JTextField edtUserId = new JTextField(20);
	private JPasswordField edtPwd = new JPasswordField(20);
	private JPasswordField edtPwd2 = new JPasswordField(20);
	public FrmRegister(Dialog f, String s, boolean b) {
		super(f, s, b);
		toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		toolBar.add(this.btnOk);
		toolBar.add(btnCancel);
		this.getContentPane().add(toolBar, BorderLayout.SOUTH);
		workPane.add(labelUser);
		workPane.add(edtUserId);
		workPane.add(labelPwd);
		workPane.add(edtPwd);
		workPane.add(labelPwd2);
		workPane.add(edtPwd2);
		this.getContentPane().add(workPane, BorderLayout.CENTER);
		this.setSize(300, 180);
		this.btnCancel.addActionListener(this);
		this.btnOk.addActionListener(this);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==this.btnCancel)
			this.setVisible(false);
		else if(e.getSource()==this.btnOk){
			String userid=this.edtUserId.getText();
			String pwd1=new String(this.edtPwd.getPassword());
			String pwd2=new String(this.edtPwd2.getPassword());
			try {
				BeanUser user=PersonPlanUtil.userManager.reg(userid,pwd1,pwd2);
				this.setVisible(false);
			} catch (BaseException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(),"错误",JOptionPane.ERROR_MESSAGE);
				return;
			}
			
		}
			
		
	}


}

```

## BaseException.java

```java
package cn.edu.zucc.personplan.util;

public class BaseException  extends Exception {
	public BaseException(String msg){
		super(msg);
	}
}

```

## BusinessException.java

```java
package cn.edu.zucc.personplan.util;

public class BusinessException extends BaseException {
	public BusinessException(String msg){
		super(msg);
	}
}

```

## DBUtil.java

```java
package cn.edu.zucc.personplan.util;

import java.sql.Connection;

public class DBUtil {
	private static final String jdbcUrl="jdbc:mysql://localhost:3306/booklib";
	private static final String dbUser="root";
	private static final String dbPwd="zucc";
	static{
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Connection getConnection() throws java.sql.SQLException{
		return java.sql.DriverManager.getConnection(jdbcUrl, dbUser, dbPwd);
	}
}

```

## DbException.java

```java
package cn.edu.zucc.personplan.util;

public class DbException extends BaseException {
	public DbException(java.lang.Throwable ex){
		super("数据库操作错误："+ex.getMessage());
	}
}

```
