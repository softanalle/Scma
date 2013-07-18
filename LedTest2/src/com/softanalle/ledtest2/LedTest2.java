package com.softanalle.ledtest2;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.softanalle.ledtest2.R;

public class LedTest2 extends IOIOActivity {
	private TextView textView1_;
	private TextView textView2_;
	private TextView textView3_;
	private SeekBar seekBar1_;
	private SeekBar seekBar2_;
	private SeekBar seekBar3_;
	private ToggleButton toggleButton_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textView1_ = (TextView) findViewById(R.id.Channel1View);
		seekBar1_ = (SeekBar) findViewById(R.id.Channel1Bar);
		
		textView2_ = (TextView) findViewById(R.id.Channel2View);
		seekBar2_ = (SeekBar) findViewById(R.id.Channel2Bar);
		
		textView3_ = (TextView) findViewById(R.id.Channel3View);
		seekBar3_ = (SeekBar) findViewById(R.id.Channel3Bar);
		
		toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);

		enableUi(false);
	}

	class Looper extends BaseIOIOLooper {
		private AnalogInput input_;
		private PwmOutput pwmOutput1_;
		private PwmOutput pwmOutput2_;
		private PwmOutput pwmOutput3_;
		private DigitalOutput led_;
		private DigitalOutput powout_;

		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			powout_ = ioio_.openDigitalOutput(6, true);
			//input_ = ioio_.openAnalogInput(40);
			
			pwmOutput1_ = ioio_.openPwmOutput(12, 100);
			pwmOutput2_ = ioio_.openPwmOutput(11, 100);
			pwmOutput3_ = ioio_.openPwmOutput(10, 100);
			
			pwmOutput1_.setPulseWidth( 0 );
			pwmOutput2_.setPulseWidth( 0 );
			pwmOutput3_.setPulseWidth( 0 );
			
			powout_.write( false );
			enableUi(true);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			int pulseWidth1 = seekBar1_.getProgress();
			int pulseWidth2 = seekBar2_.getProgress();
			int pulseWidth3 = seekBar3_.getProgress();
			//setNumber(input_.read());
			setNumber( 1, pulseWidth1 );
			setNumber( 2, pulseWidth2 );
			setNumber( 3, pulseWidth3 );
			pwmOutput1_.setPulseWidth( pulseWidth1 );
			pwmOutput2_.setPulseWidth( pulseWidth2 );
			pwmOutput3_.setPulseWidth( pulseWidth3 );
			led_.write(!toggleButton_.isChecked());
			powout_.write(toggleButton_.isChecked());
			Thread.sleep(10);
		}

		@Override
		public void disconnected() {
			enableUi(false);
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				seekBar1_.setEnabled(enable);
				seekBar2_.setEnabled(enable);
				seekBar3_.setEnabled(enable);
				toggleButton_.setEnabled(enable);
			}
		});
	}

	private void setNumber(int channel, float f) {
		final String str = String.format("%.2f", f);
		final int ch = channel;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switch(ch) {
				case 1:
					textView1_.setText(str);
					break;
				case 2:
					textView2_.setText(str);
					break;
				case 3:
					textView3_.setText(str);
					break;
				default:
					// no action
				}				
			}
		});
	}
}