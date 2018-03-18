import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class program {
	public static void main(String[] args) {
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			try {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			} catch (Exception e) {}
		}
		int size = 5;
		if (args.length > 0) {
			try {
				size = Integer.parseInt(args[0]);
			} catch (Exception e) {}
		}
		Game game = new Game(size);
	}
}

class GameFrame extends JFrame {
	JButton[][] gameBtns;
	JButton[] controlBtns;

	GameFrame(Game game) {
		int N = game.SIZE;
		gameBtns = new JButton[N][N];
		controlBtns = new JButton[4];
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		int panelSize = N * 80 + 2 * 40;
		setLayout(null);
		getContentPane().setPreferredSize(new Dimension(panelSize, panelSize));
		pack();
		MouseAdapter handler = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				game.press(((JButton) e.getSource()).getName());
			}
		};
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				// Game: N x N
				JButton btn = new JButton();
				btn.setLocation(new Point(40 + 80 * j, 40 + 80 * i));
				btn.setSize(80, 80);
				btn.setMargin(new Insets(0, 0, 0, 0));
				btn.setFont(new Font("Consolas", Font.PLAIN, 72));
				btn.setText("");
				btn.setName(String.format("g:%d:%d", i, j));
				if (game.getSide(i, j) > 0) {
					btn.addMouseListener(handler);
				}
				gameBtns[i][j] = btn;
				add(btn);
			}
		}

		JButton btn;

		// Control Top: N
		btn = new JButton();
		btn.setLocation(new Point(40, 0));
		btn.setSize(80 * N, 40);
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setFont(new Font("Consolas", Font.PLAIN, 20));
		btn.setText("v");
		btn.setName(String.format("c:%d:%d", 0, 0));
		btn.addMouseListener(handler);
		btn.setEnabled(false);
		controlBtns[0] = btn;
		add(btn);

		// Control Bottom: N
		btn = new JButton();
		btn.setLocation(new Point(40, 40 + 80 * N));
		btn.setSize(80 * N, 40);
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setFont(new Font("Consolas", Font.PLAIN, 20));
		btn.setText("^");
		btn.setName(String.format("c:%d:%d", 1, 0));
		btn.addMouseListener(handler);
		btn.setEnabled(false);
		controlBtns[1] = btn;
		add(btn);

		// Control Left: N
		btn = new JButton();
		btn.setLocation(new Point(0, 40));
		btn.setSize(40, 80 * N);
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setFont(new Font("Consolas", Font.PLAIN, 20));
		btn.setText(">");
		btn.setName(String.format("c:%d:%d", 2, 0));
		btn.addMouseListener(handler);
		btn.setEnabled(false);
		controlBtns[2] = btn;
		add(btn);

		// Control Right: N
		btn = new JButton();
		btn.setLocation(new Point(40 + 80 * N, 40));
		btn.setSize(40, 80 * N);
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setFont(new Font("Consolas", Font.PLAIN, 20));
		btn.setText("<");
		btn.setName(String.format("c:%d:%d", 3, 0));
		btn.addMouseListener(handler);
		btn.setEnabled(false);
		controlBtns[3] = btn;
		add(btn);
		
		setVisible(true);
	}
}

class Game {
	GameFrame frame;
	int SIZE;
	int[][] state;
	int newState = 1;
	boolean selected = false;
	int selected_row = -1;
	int selected_col = -1;

	Game(int size) {
		SIZE = size;
		state = new int[SIZE][SIZE];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				state[i][j] = 2;
			}
		}
		frame = new GameFrame(this);
	}

	void press(String id) {
		String[] arg = id.split(":");
		int a0 = Integer.parseInt(arg[1]);
		int a1 = Integer.parseInt(arg[2]);
		int s = -1;
		switch (arg[0]) {
			case "c":
				System.out.printf("[%s:control] %-2d %-2d\n", arg[0], a0, a1);
				if (! selected || ! frame.controlBtns[a0].isEnabled()) {
					System.out.println("fuck");
					return;
				}
				setState(selected_row, selected_col, newState);
				newState ^= 1;
				switch (a0 & 2) {
					case 0:
						switch (a0 & 1) {
							case 0: s = topInsert(); break;
							case 1: s = downInsert(); break;
						}
						if (s == -1) {
							s = checkVertical(selected_row, selected_col);
						}
						break;
					case 2:
						switch (a0 & 1) {
							case 0: s = leftInsert(); break;
							case 1: s = rightInsert(); break;
						}
						if (s == -1) {
							s = checkHorizontal(selected_row, selected_col);
						}
						break;
				}
				break;
			case "g":
				System.out.printf("[%s:goto] %-2d %-2d\n", arg[0], a0, a1);
				int sel = state[a0][a1];
				if (sel == 1 - newState) {
					System.out.println("fuck");
					return;
				}
				if (selected) {
					if (a0 == selected_row && a1 == selected_col) {
						setState(a0, a1, state[a0][a1]);
						return;
					} else {
						setState(selected_row, selected_col, state[selected_row][selected_col]);
					}
				}
				selected_row = a0;
				selected_col = a1;
				setState(a0, a1, newState, true);
				break;
		}
		if (s != -1) {
			result(s);
		}
	}

	int check(int row, int col) {
		return check(row, col, false, false);
	}
	int check(int row, int col, boolean isHorizontalRotated, boolean isVerticalRotated) {
		int s;
		s = (isHorizontalRotated? -1: checkHorizontal(row, col));
		if (s != -1) {
			return s;
		}
		s = (isVerticalRotated? -1: checkVertical(row, col));
		if (s != -1) {
			return s;
		}
		if (row == col) {
			s = checkBackSlash(row, col);
			if (s != -1) {
				return s;
			}
		}
		if (row == SIZE - col - 1) {
			s = checkForwardSlash(row, col);
			if (s != -1) {
				return s;
			}
		}
		return -1;
	}
	
	void result(int state) {
		JOptionPane.showMessageDialog(frame, String.format("[%c] WIN!!1", state == 1? 'O': 'X'));
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}
	
	int getSide(int row, int col) {
		return (row == 0? 1: 0) | (row == SIZE - 1? 2: 0) | (col == 0? 4: 0) | (col == SIZE - 1? 8: 0);
	}

	void setState(int row, int col, int s) {
		setState(row, col, s, false);
	}

	void setState(int row, int col, int s, boolean sel) {
		System.out.printf("%d, %d, %d\n", row, col, s);
		JButton btn = frame.gameBtns[row][col];
		switch (s) {
			case 0: btn.setText("X"); break;
			case 1: btn.setText("O"); break;
			case 2: btn.setText(""); break;
		}
		if (! sel) {
			state[row][col] = s;
		}
		selected = sel;
		btn.setBackground(sel? Color.CYAN: null);
		if (sel) {
			for (JButton cbtn: frame.controlBtns) {
				cbtn.setEnabled(true);
			}
			int side = getSide(row, col);
			for (int i = 0; i < 4; i++) {
				if ((side & (int) Math.pow(2, i)) > 0) {
					frame.controlBtns[i].setEnabled(false);
				}
			}
		} else {
			for (JButton cbtn: frame.controlBtns) {
				cbtn.setEnabled(false);
			}
		}
	}

	int leftInsert() {
		int row = selected_row;
		int col = selected_col;
		int tmp = state[row][col];
		int s = -1;
		for (int i = col; i >= 0; i--) {
			setState(row, i, (i == 0? tmp: state[row][i - 1]));
			if (s != -1) {
				continue;
			}
			s = check(row, i, true, false);
		}
		return s;
	}

	int rightInsert() {
		int row = selected_row;
		int col = selected_col;
		int tmp = state[row][col];
		int s = -1;
		for (int i = col; i < SIZE; i++) {
			setState(row, i, (i == SIZE - 1? tmp: state[row][i + 1]));
			if (s != -1) {
				continue;
			}
			s = check(row, i, true, false);
		}
		return s;
	}

	int downInsert() {
		int row = selected_row;
		int col = selected_col;
		int tmp = state[row][col];
		int s = -1;
		for (int i = row; i < SIZE; i++) {
			setState(i, col, (i == SIZE - 1? tmp: state[i + 1][col]));
			if (s != -1) {
				continue;
			}
			s = check(i, col, false, true);
		}
		return s;
	}

	int topInsert() {
		int row = selected_row;
		int col = selected_col;
		int tmp = state[row][col];
		int s = -1;
		for (int i = row; i >= 0; i--) {
			setState(i, col, (i == 0? tmp: state[i - 1][col]));
			if (s != -1) {
				continue;
			}
			s = check(i, col, false, true);
		}
		return s;
	}

	int checkHorizontal(int row, int col) {
		int s = state[row][0];
		for (int i = 0; i < SIZE; i++) {
			if (state[row][i] != s) {
				return -1;
			}
		}
		return (s == 2? -1: s);
	}
	
	int checkVertical(int row, int col) {
		int s = state[0][col];
		for (int i = 0; i < SIZE; i++) {
			if (state[i][col] != s) {
				return -1;
			}
		}
		return (s == 2? -1: s);
	}
	
	int checkBackSlash(int row, int col) {
		int s = state[0][0];
		for (int i = 0; i < SIZE; i++) {
			if (state[i][i] != s) {
				return -1;
			}
		}
		return (s == 2? -1: s);
	}
	
	int checkForwardSlash(int row, int col) {
		int s = state[0][SIZE - 1];
		for (int i = 0; i < SIZE; i++) {
			if (state[i][SIZE - i - 1] != s) {
				return -1;
			}
		}
		return (s == 2? -1: s);
	}

}
