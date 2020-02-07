/*
 * Copyright (c) 2017 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.icam.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.icam.R;
import com.icam.customUI.GraphicOverlay;
import com.icam.models.FaceData;
import com.icam.models.FaceEmotions;


class FaceGraphic extends GraphicOverlay.Graphic {

    private static final int PIXEL_WIDTH = 48;

    private TensorFlowClassifier classifier;
    private static final String TAG = "FaceGraphic";

    private static final float DOT_RADIUS = 3.0f;
    private static final float TEXT_OFFSET_Y = -30.0f;

    private boolean mIsFrontFacing;

    // This variable may be written to by one of many threads. By declaring it as volatile,
    // we guarantee that when we read its contents, we're reading the most recent "write"
    // by any thread.
    private volatile FaceData mFaceData;

    private Paint mHintTextPaint;
    private Paint mHintOutlinePaint;
    private Paint mEyeWhitePaint;
    private Paint mIrisPaint;
    private Paint mEyeOutlinePaint;
    private Paint mEyelidPaint;

    private Drawable mPigNoseGraphic;
    private Drawable mMustacheGraphic;
    private Drawable mHappyStarGraphic;
    private Drawable mHatGraphic;
    private Drawable angryGraphic;
    private Drawable disgustGraphic;
    private Drawable fearGraphic;
    private Drawable happyGraphic;
    private Drawable sadGraphic;
    private Drawable surprisedGraphic;

    // We want each iris to move independently, so each one gets its own physics engine.
    private EyePhysics mLeftPhysics = new EyePhysics();
    private EyePhysics mRightPhysics = new EyePhysics();


    FaceGraphic(GraphicOverlay overlay, Context context, boolean isFrontFacing) {
        super(overlay);
        mIsFrontFacing = isFrontFacing;
        Resources resources = context.getResources();
        initializePaints(resources);
        initializeGraphics(resources);
    }

    private void initializeGraphics(Resources resources) {
        mPigNoseGraphic = resources.getDrawable(R.drawable.pig_nose_emoji);
        mMustacheGraphic = resources.getDrawable(R.drawable.mustache);
        mHappyStarGraphic = resources.getDrawable(R.drawable.happy_star);
        mHatGraphic = resources.getDrawable(R.drawable.red_hat);
        angryGraphic = resources.getDrawable(R.drawable.angry);
        disgustGraphic = resources.getDrawable(R.drawable.disgust);
        fearGraphic = resources.getDrawable(R.drawable.fear);
        happyGraphic = resources.getDrawable(R.drawable.happy);
        sadGraphic = resources.getDrawable(R.drawable.sad);
        surprisedGraphic = resources.getDrawable(R.drawable.suprised);

    }

    private void initializePaints(Resources resources) {
        mHintTextPaint = new Paint();
        mHintTextPaint.setColor(resources.getColor(R.color.overlayHint));
        mHintTextPaint.setTextSize(resources.getDimension(R.dimen.textSize));

        mHintOutlinePaint = new Paint();
        mHintOutlinePaint.setColor(resources.getColor(R.color.overlayHint));
        mHintOutlinePaint.setStyle(Paint.Style.STROKE);
        mHintOutlinePaint.setStrokeWidth(resources.getDimension(R.dimen.hintStroke));

        mEyeWhitePaint = new Paint();
        mEyeWhitePaint.setColor(resources.getColor(R.color.eyeWhite));
        mEyeWhitePaint.setStyle(Paint.Style.FILL);

        mIrisPaint = new Paint();
        mIrisPaint.setColor(resources.getColor(R.color.iris));
        mIrisPaint.setStyle(Paint.Style.FILL);

        mEyeOutlinePaint = new Paint();
        mEyeOutlinePaint.setColor(resources.getColor(R.color.eyeOutline));
        mEyeOutlinePaint.setStyle(Paint.Style.STROKE);
        mEyeOutlinePaint.setStrokeWidth(resources.getDimension(R.dimen.eyeOutlineStroke));

        mEyelidPaint = new Paint();
        mEyelidPaint.setColor(resources.getColor(R.color.eyelid));
        mEyelidPaint.setStyle(Paint.Style.FILL);
    }

    void update(FaceData face) {
        mFaceData = face;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mFaceData.hasEmotionData()) {
            drawEmoji(canvas);
        } else {
            drawCartoon(canvas);
        }
    }

    private void drawEmoji(Canvas canvas){
        if (mFaceData == null)
            return;
        Drawable emoji = null;
        switch (mFaceData.getEmotion()) {
            case FaceEmotions.ANGRY:
                emoji = angryGraphic;
                break;
            case FaceEmotions.DISGUST:
                emoji = disgustGraphic;
                break;
            case FaceEmotions.FEAR:
                emoji = fearGraphic;
                break;
            case FaceEmotions.HAPPY:
                emoji = happyGraphic;
                break;
            case FaceEmotions.SAD:
                emoji = sadGraphic;
                break;
            case FaceEmotions.SUPRISE:
                emoji = surprisedGraphic;
                break;
        }
        if (emoji == null)
            return;
        Log.i(TAG, "emotion: "+mFaceData.getEmotion());
        float centerX = translateX(mFaceData.getPosition().x + mFaceData.getWidth() / 2.0f);
        float centerY = translateY(mFaceData.getPosition().y + mFaceData.getHeight() / 2.0f);
        float offsetX = scaleX(mFaceData.getWidth() / 2.0f);
        float offsetY = scaleY(mFaceData.getHeight() / 2.0f);

        // draw a box around the face
        int left = Math.round(centerX - offsetX);
        int right = Math.round(centerX + offsetX);
        int top = Math.round(centerY - offsetY);
        int bottom = Math.round(centerY + offsetY);

        emoji.setBounds(left, top, right, bottom);
        emoji.draw(canvas);

        float pointX = translateX(mFaceData.getPosition().x);
        float pointY = translateY(mFaceData.getPosition().y);
        canvas.drawCircle(pointX, pointY, DOT_RADIUS, mHintOutlinePaint);
        canvas.drawText(mFaceData.getEmotion()+" : "+mFaceData.getEmotionCoef(), pointX,
                pointY + TEXT_OFFSET_Y, mHintTextPaint);

    }

    private void drawCartoon(Canvas canvas) {
        // Confirm that the face and its features are still visible
        // before drawing any graphics over it.
        if (mFaceData == null) {
            return;
        }

        PointF detectPosition = mFaceData.getPosition();
        PointF detectLeftEyePosition = mFaceData.getLeftEyePosition();
        PointF detectRightEyePosition = mFaceData.getRightEyePosition();
        PointF detectNoseBasePosition = mFaceData.getNoseBasePosition();
        PointF detectMouthLeftPosition = mFaceData.getMouthLeftPosition();
        PointF detectMouthBottomPosition = mFaceData.getMouthBottomPosition();
        PointF detectMouthRightPosition = mFaceData.getMouthRightPosition();

        if ((detectPosition == null) ||
                (detectLeftEyePosition == null) ||
                (detectRightEyePosition == null) ||
                (detectNoseBasePosition == null) ||
                (detectMouthLeftPosition == null) ||
                (detectMouthBottomPosition == null) ||
                (detectMouthRightPosition == null)) {
            return;
        }

        // Face position and dimensions
        PointF position = new PointF(translateX(detectPosition.x),
                translateY(detectPosition.y));
        float width = scaleX(mFaceData.getWidth());
        float height = scaleY(mFaceData.getHeight());

        // Eye coordinates
        PointF leftEyePosition = new PointF(translateX(detectLeftEyePosition.x),
                translateY(detectLeftEyePosition.y));
        PointF rightEyePosition = new PointF(translateX(detectRightEyePosition.x),
                translateY(detectRightEyePosition.y));

        // Eye state
        boolean leftEyeOpen = mFaceData.isLeftEyeOpen();
        boolean rightEyeOpen = mFaceData.isRightEyeOpen();

        // Nose coordinates
        PointF noseBasePosition = new PointF(translateX(detectNoseBasePosition.x),
                translateY(detectNoseBasePosition.y));

        // Mouth coordinates
        PointF mouthLeftPosition = new PointF(translateX(detectMouthLeftPosition.x),
                translateY(detectMouthLeftPosition.y));
        PointF mouthRightPosition = new PointF(translateX(detectMouthRightPosition.x),
                translateY(detectMouthRightPosition.y));
        PointF mouthBottomPosition = new PointF(translateX(detectMouthBottomPosition.x),
                translateY(detectMouthBottomPosition.y));

        // Smile state
        boolean smiling = mFaceData.isSmiling();

        // Calculate the distance between the eyes using Pythagoras' formula,
        // and we'll use that distance to set the size of the eyes and irises.
        final float EYE_RADIUS_PROPORTION = 0.45f;
        final float IRIS_RADIUS_PROPORTION = EYE_RADIUS_PROPORTION / 2.0f;
        float distance = (float) Math.sqrt(
                (rightEyePosition.x - leftEyePosition.x) * (rightEyePosition.x - leftEyePosition.x) +
                        (rightEyePosition.y - leftEyePosition.y) * (rightEyePosition.y - leftEyePosition.y));
        float eyeRadius = EYE_RADIUS_PROPORTION * distance;
        float irisRadius = IRIS_RADIUS_PROPORTION * distance;

        // Draw the eyes.
        PointF leftIrisPosition = mLeftPhysics.nextIrisPosition(leftEyePosition, eyeRadius, irisRadius);
        drawEye(canvas, leftEyePosition, eyeRadius, leftIrisPosition, irisRadius, leftEyeOpen, smiling);
        PointF rightIrisPosition = mRightPhysics.nextIrisPosition(rightEyePosition, eyeRadius, irisRadius);
        drawEye(canvas, rightEyePosition, eyeRadius, rightIrisPosition, irisRadius, rightEyeOpen, smiling);

        // Draw the nose.
        drawNose(canvas, noseBasePosition, leftEyePosition, rightEyePosition, width);

        // Draw the mustache.
        drawMustache(canvas, noseBasePosition, mouthLeftPosition, mouthRightPosition);
        // Head tilt
        float eulerY = mFaceData.getEulerY();
        float eulerZ = mFaceData.getEulerZ();

        // Draw the hat only if the subject's head is titled at a sufficiently jaunty angle.
        final float HEAD_TILT_HAT_THRESHOLD = 20.0f;
        if (Math.abs(eulerZ) > HEAD_TILT_HAT_THRESHOLD) {
            drawHat(canvas, position, width, height, noseBasePosition);
        }
    }

    private void drawEye(Canvas canvas,
                         PointF eyePosition, float eyeRadius,
                         PointF irisPosition, float irisRadius,
                         boolean eyeOpen, boolean smiling) {
        if (eyeOpen) {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeWhitePaint);
            if (smiling) {
                mHappyStarGraphic.setBounds(
                        (int) (irisPosition.x - irisRadius),
                        (int) (irisPosition.y - irisRadius),
                        (int) (irisPosition.x + irisRadius),
                        (int) (irisPosition.y + irisRadius));
                mHappyStarGraphic.draw(canvas);
            } else {
                canvas.drawCircle(irisPosition.x, irisPosition.y, irisRadius, mIrisPaint);
            }
        } else {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyelidPaint);
            float y = eyePosition.y;
            float start = eyePosition.x - eyeRadius;
            float end = eyePosition.x + eyeRadius;
            canvas.drawLine(start, y, end, y, mEyeOutlinePaint);
        }
        canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeOutlinePaint);
    }

    private void drawNose(Canvas canvas,
                          PointF noseBasePosition,
                          PointF leftEyePosition, PointF rightEyePosition,
                          float faceWidth) {
        final float NOSE_FACE_WIDTH_RATIO = (float) (1 / 5.0);
        float noseWidth = faceWidth * NOSE_FACE_WIDTH_RATIO;
        int left = (int) (noseBasePosition.x - (noseWidth / 2));
        int right = (int) (noseBasePosition.x + (noseWidth / 2));
        int top = (int) (leftEyePosition.y + rightEyePosition.y) / 2;
        int bottom = (int) noseBasePosition.y;

        mPigNoseGraphic.setBounds(left, top, right, bottom);
        mPigNoseGraphic.draw(canvas);
    }

    private void drawMustache(Canvas canvas,
                              PointF noseBasePosition,
                              PointF mouthLeftPosition, PointF mouthRightPosition) {
        int left = (int) mouthLeftPosition.x;
        int top = (int) noseBasePosition.y;
        int right = (int) mouthRightPosition.x;
        int bottom = (int) Math.min(mouthLeftPosition.y, mouthRightPosition.y);

        if (mIsFrontFacing) {
            mMustacheGraphic.setBounds(left, top, right, bottom);
        } else {
            mMustacheGraphic.setBounds(right, top, left, bottom);
        }
        mMustacheGraphic.draw(canvas);
    }

    private void drawHat(Canvas canvas, PointF facePosition, float faceWidth, float faceHeight, PointF noseBasePosition) {
        final float HAT_FACE_WIDTH_RATIO = (float) (1.0 / 4.0);
        final float HAT_FACE_HEIGHT_RATIO = (float) (1.0 / 6.0);
        final float HAT_CENTER_Y_OFFSET_FACTOR = (float) (1.0 / 8.0);

        float hatCenterY = facePosition.y + (faceHeight * HAT_CENTER_Y_OFFSET_FACTOR);
        float hatWidth = faceWidth * HAT_FACE_WIDTH_RATIO;
        float hatHeight = faceHeight * HAT_FACE_HEIGHT_RATIO;

        int left = (int) (noseBasePosition.x - (hatWidth / 2));
        int right = (int) (noseBasePosition.x + (hatWidth / 2));
        int top = (int) (hatCenterY - (hatHeight / 2));
        int bottom = (int) (hatCenterY + (hatHeight / 2));
        mHatGraphic.setBounds(left, top, right, bottom);
        mHatGraphic.draw(canvas);
    }

    private void labelLandmarks(Canvas canvas) {
        final float DOT_RADIUS = 3.0f;
        final float TEXT_OFFSET_Y = -30.0f;

        // Confirm that the face and its features are still visible before drawing any graphics over it.
        if (mFaceData == null) {
            return;
        }

        // 1
        PointF detectPosition = mFaceData.getPosition();
        PointF detectLeftEyePosition = mFaceData.getLeftEyePosition();
        PointF detectRightEyePosition = mFaceData.getRightEyePosition();
        PointF detectNoseBasePosition = mFaceData.getNoseBasePosition();
        PointF detectMouthLeftPosition = mFaceData.getMouthLeftPosition();
        PointF detectMouthBottomPosition = mFaceData.getMouthBottomPosition();
        PointF detectMouthRightPosition = mFaceData.getMouthRightPosition();
        if ((detectPosition == null) ||
                (detectLeftEyePosition == null) ||
                (detectRightEyePosition == null) ||
                (detectNoseBasePosition == null) ||
                (detectMouthLeftPosition == null) ||
                (detectMouthBottomPosition == null) ||
                (detectMouthRightPosition == null)) {
            return;
        }

        // 2
        float leftEyeX = translateX(detectLeftEyePosition.x);
        float leftEyeY = translateY(detectLeftEyePosition.y);
        canvas.drawCircle(leftEyeX, leftEyeY, DOT_RADIUS, mHintOutlinePaint);
        canvas.drawText("left eye", leftEyeX, leftEyeY + TEXT_OFFSET_Y, mHintTextPaint);

        float rightEyeX = translateX(detectRightEyePosition.x);
        float rightEyeY = translateY(detectRightEyePosition.y);
        canvas.drawCircle(rightEyeX, rightEyeY, DOT_RADIUS, mHintOutlinePaint);
        canvas.drawText("right eye", rightEyeX, rightEyeY + TEXT_OFFSET_Y, mHintTextPaint);

        float noseBaseX = translateX(detectNoseBasePosition.x);
        float noseBaseY = translateY(detectNoseBasePosition.y);
        canvas.drawCircle(noseBaseX, noseBaseY, DOT_RADIUS, mHintOutlinePaint);
        canvas.drawText("nose base", noseBaseX, noseBaseY + TEXT_OFFSET_Y, mHintTextPaint);

        float mouthLeftX = translateX(detectMouthLeftPosition.x);
        float mouthLeftY = translateY(detectMouthLeftPosition.y);
        canvas.drawCircle(mouthLeftX, mouthLeftY, DOT_RADIUS, mHintOutlinePaint);
        canvas.drawText("mouth left", mouthLeftX, mouthLeftY + TEXT_OFFSET_Y, mHintTextPaint);

        float mouthRightX = translateX(detectMouthRightPosition.x);
        float mouthRightY = translateY(detectMouthRightPosition.y);
        canvas.drawCircle(mouthRightX, mouthRightY, DOT_RADIUS, mHintOutlinePaint);
        canvas.drawText("mouth right", mouthRightX, mouthRightY + TEXT_OFFSET_Y, mHintTextPaint);

        float mouthBottomX = translateX(detectMouthBottomPosition.x);
        float mouthBottomY = translateY(detectMouthBottomPosition.y);
        canvas.drawCircle(mouthBottomX, mouthBottomY, DOT_RADIUS, mHintOutlinePaint);
        canvas.drawText("mouth bottom", mouthBottomX, mouthBottomY + TEXT_OFFSET_Y, mHintTextPaint);
    }

    private void drawRect(Canvas canvas) {
        FaceData face = mFaceData;
        if (face == null)
            return;

        float centerX = translateX(face.getPosition().x + face.getWidth() / 2.0f);
        float centerY = translateY(face.getPosition().y + face.getHeight() / 2.0f);
        float offsetX = scaleX(face.getWidth() / 2.0f);
        float offsetY = scaleY(face.getHeight() / 2.0f);

        // draw a box around the face
        float left = centerX - offsetX;
        float right = centerX + offsetX;
        float top = centerY - offsetY;
        float bottom = centerY + offsetY;

        canvas.drawRect(left, top, right, bottom, mHintOutlinePaint);
        //draw face id
        canvas.drawText(String.format("id: %d", face.getId()), centerX, centerY, mHintTextPaint);
    }

}
