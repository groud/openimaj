/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos.sandbox.video;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *  Tracks faces in video using the CLM Tracker.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@version $Author$, $Revision$, $Date$
 *	@created 4 Jul 2012
 */
public class VideoFaceTracker implements VideoDisplayListener<MBFImage>
{
	/** The shot detector to use */
	private VideoShotDetector shotDetector = null; 

	/** The face tracker we're going to use */
	private CLMFaceTracker tracker = new CLMFaceTracker();

	/** Frame counter for FPS display */
	private int fnum = 0;
	
	/** Cache for the FPS text generated every 10 frames */
	private String fpsText = "";

	/** Timers for the FPS calculation */
	private long t1, t0 = System.currentTimeMillis();

	/** Whether to show the FPS on the view */
	private boolean showFPS = true;

	/** Whether to draw triangles on the video */
	private boolean drawTriangles = false;

	/** Whether to draw connection on the video */
	private boolean drawConnections = true;

	/** Whether to draw points on the video */
	private boolean drawPoints = true;
	
	/** Whether to draw the face bounds */
	private boolean drawBounds = true;
	
	/** Whether to draw the template match search area */
	private boolean drawSearchArea = true;

	/**
	 * 	Default constructor
	 * 	@param v The video to track faces in 
	 *	@throws IOException
	 */
	public VideoFaceTracker( Video<MBFImage> v ) throws IOException
	{
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( v );
		vd.addVideoListener( this );
		shotDetector = new VideoShotDetector( v.getFPS() );
//		shotDetector.setThreshold( 500 );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
	public void beforeUpdate( MBFImage frame )
	{
		// Process the frame.
		shotDetector.processFrame( frame );
		if( shotDetector.wasLastFrameBoundary() )
		{
			System.out.println( "Shot detected. Forcing redetect on face tracker.");
			tracker.reset();
		}

		// Track faces in the image
		tracker.track( frame );
		
		// Draw the tracked model to the image
		tracker.drawModel( frame, drawTriangles, drawConnections, drawPoints,
				drawSearchArea, drawBounds );
	
		// Whether to show FPS
		if( showFPS )
		{
			// Draw framerate on display image (average of 10 frames)
			if( fnum >= 9 )
			{
				t1 = System.currentTimeMillis();
				double fps = 10 / ((double)(t1 - t0) / 1000.0);
				t0 = t1;
				fnum = 0;
				fpsText = String.format( "%d frames/sec", (int)Math.round( fps ) );
			}
			else
			{
				fnum++;
			}

			frame.drawText( fpsText, 10, 20, HersheyFont.ROMAN_SIMPLEX, 20,
			        RGBColour.GREEN );
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void afterUpdate( VideoDisplay<MBFImage> display )
	{
		// No implementation
	}

	/**
	 *	@param args
	 *	@throws Exception
	 */
	public static void main( String[] args ) throws Exception
	{
		new VideoFaceTracker( new XuggleVideo( new File( "/home/dd/rt20111114.mp4" ) ) );
	}
}
