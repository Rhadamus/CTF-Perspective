package Extensions;

import Actions.CActExtension;
import Application.CRunApp;
import Banks.CImage;
import Conditions.CCndExtension;
import Expressions.CValue;
import Objects.CObject;
import OpenGL.GLRenderer;
import RunLoop.CCreateObjectInfo;
import Runtime.MMFRuntime;
import Runtime.SurfaceView;
import Services.CBinaryFile;
import Services.CServices;
import Sprites.CRSpr;

import android.graphics.Bitmap;

public class CRunPerspective extends CRunExtension {
    public static final int ACTSETZOOMVALUE = 0;
    public static final int ACTSETPANORAMA = 1;
    public static final int ACTSETPERSPECTIVE = 2;
    public static final int ACTSETSINEWAVE = 3;
    public static final int ACTSETCUSTOM = 4;
    public static final int ACTSETNUMWAVES = 5;
    public static final int ACTSETOFFSET = 6;
    public static final int ACTSETHORIZONTAL = 7;
    public static final int ACTSETVERTICAL = 8;
    public static final int ACTSETLEFTTOP = 9;
    public static final int ACTSETRIGHTBOTTOM = 10;
    public static final int ACTSETCUSTOMVALUE = 11;
    public static final int ACTSETWIDTH = 12;
    public static final int ACTSETHEIGHT = 13;
    public static final int ACTSETRESAMPLEON = 14;
    public static final int ACTSETRESAMPLEOFF = 15;
    public static final int ACTSETSINEOFFSET = 16;
    public static final int ACTSETCUSTOMOFFSET = 17;

    public static final int EXPGETZOOMVALUE = 0;
    public static final int EXPGETOFFSET = 1;
    public static final int EXPNUMWAVES = 2;
    public static final int EXPGETCUSTOM = 3;
    public static final int EXPGETWIDTH = 4;
    public static final int EXPGETHEIGHT = 5;

    public static final int PANORAMA = 0;
    public static final int PERSPECTIVE = 1;
    public static final int SINEWAVE = 2;
    public static final int SINEOFFSET = 3;
    //public static final int CUSTOM = 4;
    //public static final int CUSTOMOFFSET = 5;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static final int LEFTBOTTOM = 0;
    public static final int RIGHTTOP = 1;

    private static int panoShader = -1;
    private static int persShader = -1;
    private static int sineShader = -1;
    private static int offsShader = -1;

    private int Effect;
    private int Direction;
    private int PerspectiveDir;
    private int ZoomValue;
    private int Offset;
    private int SineWaveWaves;
    private int[] CustomArray;
    private boolean resample;
    private double[] slope = { 0.0, 0.0 };

    private CImage imageTexture;
    private boolean paused;

    public CValue expRet = new CValue(0);

    @Override // Extensions.CRunExtension
    public int getNumberOfConditions() {
        return 0;
    }

    @Override // Extensions.CRunExtension
    public int handleRunObject() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public class CImageTexture extends CImage {
        public CImageTexture(int i, int i2) {
            Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
            allocNative2((MMFRuntime.inst.app.hdr2Options & 4096) != 0, (short)-1, CServices.getBitmapPixels(createBitmap), 0, 0, 0, 0, createBitmap.getWidth(), createBitmap.getHeight(), SurfaceView.ES);
        }

        @Override // Banks.CImage
        public void onDestroy() {
            CRunPerspective.this.imageTexture = null;
        }
    }

    @Override // Extensions.CRunExtension
    public boolean createRunObject(CBinaryFile cBinaryFile, CCreateObjectInfo cCreateObjectInfo, int i) {
        cBinaryFile.skipBytes(4);
        short readShort = cBinaryFile.readShort();
        short readShort2 = cBinaryFile.readShort();
        this.ho.setX(cCreateObjectInfo.cobX);
        this.ho.setY(cCreateObjectInfo.cobY);
        this.ho.setWidth(readShort);
        this.ho.setHeight(readShort2);
        this.Effect = cBinaryFile.readByte();
        this.Direction = cBinaryFile.readByte() != 0 ? VERTICAL : HORIZONTAL;
        cBinaryFile.skipBytes(2);
        this.ZoomValue = cBinaryFile.readInt();
        this.Offset = cBinaryFile.readInt();
        this.SineWaveWaves = cBinaryFile.readInt();
        this.PerspectiveDir = cBinaryFile.readByte() != 0 ? LEFTBOTTOM : RIGHTTOP;
        this.resample = cBinaryFile.readByte() != 0;
        this.ho.roc.rcChanged = true;
        int i2 = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        this.CustomArray = new int[i2];
        for (int i3 = 0; i3 < i2; i3++) {
            this.CustomArray[i3] = this.ZoomValue;
        }
        this.imageTexture = new CImageTexture(this.ho.hoImgWidth, this.ho.hoImgHeight);
        this.paused = false;
        return true;
    }

    @Override // Extensions.CRunExtension
    public void pauseRunObject() {
        this.paused = true;
        this.imageTexture.destroy();
    }

    @Override // Extensions.CRunExtension
    public void continueRunObject() {
        this.paused = false;
        this.imageTexture = new CImageTexture(this.ho.hoImgWidth, this.ho.hoImgHeight);
    }

    @Override // Extensions.CRunExtension
    public void destroyRunObject(boolean z) {
        this.CustomArray = null;

        if (imageTexture != null) imageTexture.destroy();
    }

    @Override
    public void onStop() {
        if (GLRenderer.inst != null) { synchronized (GLRenderer.inst) {
            if (panoShader > 0) GLRenderer.inst.removeShader(panoShader);
            if (persShader > 0) GLRenderer.inst.removeShader(persShader);
            if (sineShader > 0) GLRenderer.inst.removeShader(sineShader);
            if (offsShader > 0) GLRenderer.inst.removeShader(offsShader);
        } }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v10, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x023b A[Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x02ba }] */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x02f0 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x02f1  */
    @Override // Extensions.CRunExtension
    public void displayRunObject() {
        if (paused || ho.hoImgWidth == 0 || ho.hoImgHeight == 0 || (ho.ros.rsFlags & CRSpr.RSFLAG_VISIBLE) == 0) {
            return;
        }

        if (panoShader == -1) panoShader = GLRenderer.inst.addShaderFromFile("panorama_ext", new String[] {"fB", "pDir"}, true, false);
        if (persShader == -1) persShader = GLRenderer.inst.addShaderFromFile("perspective_ext", new String[] {"fA", "fB", "pDir"}, true, false);
        if (sineShader == -1) sineShader = GLRenderer.inst.addShaderFromFile("sinewave_ext", new String[] {"Zoom", "WaveIncrement", "Offset", "pDir"}, true, false);
        if (offsShader == -1) offsShader = GLRenderer.inst.addShaderFromFile("sineoffset_ext", new String[] {"Zoom", "WaveIncrement", "Offset", "pDir"}, true, false);

        int objX = ho.hoX - rh.rhWindowX;
        int objY = ho.hoY - rh.rhWindowY;
        int objWidth = ho.hoImgWidth;
        int objHeight = ho.hoImgHeight;

        GLRenderer.inst.readFrameToTexture(imageTexture, objX, objY, objWidth, objHeight);

        if (imageTexture == null) {
            return;
        }

        synchronized (imageTexture) { synchronized (GLRenderer.inst) {
            GLRenderer.inst.pushClip(objX, objY, objWidth, objHeight);

            int objSize = Direction == HORIZONTAL ? objHeight : objWidth;

            if (Effect == PANORAMA) {
                GLRenderer.inst.setEffectShader(panoShader);
                
                GLRenderer.inst.updateVariable1f("fB", Math.max(1.0f, (float)(objSize - ZoomValue)) / Math.max(1.0f, (float)objSize));
                GLRenderer.inst.updateVariable1i("pDir", Direction);
            }
            else if (Effect == PERSPECTIVE) {
                GLRenderer.inst.setEffectShader(persShader);

                if (PerspectiveDir == LEFTBOTTOM) {
                    slope[0] = ((double) objSize + 0.5) / (double) objSize;
                    slope[1] = (((double) objSize / ((double) (ZoomValue + objSize) / (double) objSize)) + 0.5) / (double) objSize;
                }
                else {
                    slope[0] = (((double) objSize / ((double) (ZoomValue + objSize) / (double) objSize)) + 0.5) / (double) objSize;
                    slope[1] = ((double) objSize + 0.5) / (double) objSize;
                }

                GLRenderer.inst.updateVariable1f("fA", (float) slope[0]);
                GLRenderer.inst.updateVariable1f("fB", (float) slope[1]);
                GLRenderer.inst.updateVariable1i("pDir", Direction);
            }
            else if (Effect == SINEWAVE) {
                GLRenderer.inst.setEffectShader(sineShader);

                int objSize2 = Direction == HORIZONTAL ? objWidth : objHeight;
                GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / (float) objSize);
                GLRenderer.inst.updateVariable1f("WaveIncrement", ((float) (SineWaveWaves * 360) / (float) objHeight) * (float) objSize2);
                GLRenderer.inst.updateVariable1f("Offset", (float) Offset);
                GLRenderer.inst.updateVariable1i("pDir", Direction);
            }
            else if (Effect == SINEOFFSET) {
                GLRenderer.inst.setEffectShader(offsShader);

                GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / (float) objHeight);
                GLRenderer.inst.updateVariable1f("WaveIncrement", ((float) (SineWaveWaves * 360) / (float) objHeight) * (float) objSize);
                GLRenderer.inst.updateVariable1f("Offset", (float) Offset);
                GLRenderer.inst.updateVariable1i("pDir", Direction);
            }

            //imageTexture.setResampling(resample);
            GLRenderer.inst.renderImage(imageTexture, resample, objX, objY, objWidth, objHeight, 0, 0);
            GLRenderer.inst.removeEffectShader();
            GLRenderer.inst.popClip();
        } }
    }

    /* access modifiers changed from: package-private */
    public int resizePerspective(int i) {
        CImage cImage = this.imageTexture;
        if (cImage != null) {
            cImage.destroy();
        }
        this.imageTexture = new CImageTexture(this.ho.hoImgWidth, this.ho.hoImgHeight);
        int i2 = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int[] iArr = new int[i2];
        int min = Math.min(i2, i);
        for (int i3 = 0; i3 < i2; i3++) {
            if (i3 < min) {
                iArr[i3] = this.CustomArray[i3];
            } else {
                iArr[i3] = 0;
            }
        }
        this.CustomArray = iArr;
        this.ho.roc.rcChanged = true;
        return 0;
    }

    @Override // Extensions.CRunExtension
    public boolean condition(int i, CCndExtension cCndExtension) {
        return false;
    }

    @Override // Extensions.CRunExtension
    public void action(int i, CActExtension cActExtension) {
        switch (i) {
            case ACTSETZOOMVALUE:
                actSetZoomValue(cActExtension);
                break;
            case ACTSETPANORAMA:
                actSetPanorama(cActExtension);
                break;
            case ACTSETPERSPECTIVE:
                actSetPerspective(cActExtension);
                break;
            case ACTSETSINEWAVE:
                actSetSineWave(cActExtension);
                break;
            case ACTSETCUSTOM:
                actSetCustom(cActExtension);
                break;
            case ACTSETNUMWAVES:
                actSetNumWaves(cActExtension);
                break;
            case ACTSETOFFSET:
                actSetOffset(cActExtension);
                break;
            case ACTSETHORIZONTAL:
                actSetHorizontal(cActExtension);
                break;
            case ACTSETVERTICAL:
                actSetVertical(cActExtension);
                break;
            case ACTSETLEFTTOP:
                actSetLeftTop(cActExtension);
                break;
            case ACTSETRIGHTBOTTOM:
                actSetRightBottom(cActExtension);
                break;
            case ACTSETCUSTOMVALUE:
                actSetCustomValue(cActExtension);
                break;
            case ACTSETWIDTH:
                actSetWidth(cActExtension);
                break;
            case ACTSETHEIGHT:
                actSetHeight(cActExtension);
                break;
            case ACTSETRESAMPLEON:
                actSetResampleOn(cActExtension);
                break;
            case ACTSETRESAMPLEOFF:
                actSetResampleOff(cActExtension);
                break;
            case ACTSETSINEOFFSET:
                actSetSineOffset(cActExtension);
                break;
            case ACTSETCUSTOMOFFSET:
                actSetCustomOffset(cActExtension);
                break;
        }
    }

    @Override // Extensions.CRunExtension
    public CValue expression(int i) {
        switch (i) {
            case EXPGETZOOMVALUE:
                return expGetZoomValue();
            case EXPGETOFFSET:
                return expGetOffset();
            case EXPNUMWAVES:
                return expNumWaves();
            case EXPGETCUSTOM:
                return expGetCustom();
            case EXPGETWIDTH:
                return expGetWidth();
            case EXPGETHEIGHT:
                return expGetHeight();
            default:
                return null;
        }
    }

    private void actSetZoomValue(CActExtension cActExtension) {
        this.ZoomValue = cActExtension.getParamExpression(this.rh, 0);
        this.ho.roc.rcChanged = true;
    }

    private void actSetPanorama(CActExtension cActExtension) {
        this.Effect = PANORAMA;
        this.ho.roc.rcChanged = true;
    }

    private void actSetPerspective(CActExtension cActExtension) {
        this.Effect = PERSPECTIVE;
        this.ho.roc.rcChanged = true;
    }

    private void actSetSineWave(CActExtension cActExtension) {
        this.Effect = SINEWAVE;
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustom(CActExtension cActExtension) {
        this.ho.roc.rcChanged = true;
    }

    private void actSetNumWaves(CActExtension cActExtension) {
        this.SineWaveWaves = cActExtension.getParamExpression(this.rh, 0);
        this.ho.roc.rcChanged = true;
    }

    private void actSetOffset(CActExtension cActExtension) {
        this.Offset = cActExtension.getParamExpression(this.rh, 0);
        this.ho.roc.rcChanged = true;
    }

    private void actSetHorizontal(CActExtension cActExtension) {
        if (Direction == VERTICAL) {
            int customSize = Math.min(ho.hoImgHeight, ho.hoImgWidth);
            int[] newCustom = new int[ho.hoImgWidth];

            if (customSize >= 0) System.arraycopy(CustomArray, 0, newCustom, 0, customSize);
            CustomArray = newCustom;
        }

        this.Direction = HORIZONTAL;
        this.ho.roc.rcChanged = true;
    }

    private void actSetVertical(CActExtension cActExtension) {
        if (Direction == HORIZONTAL) {
            int customSize = Math.min(ho.hoImgWidth, ho.hoImgHeight);
            int[] newCustom = new int[ho.hoImgHeight];

            if (customSize >= 0) System.arraycopy(CustomArray, 0, newCustom, 0, customSize);
            CustomArray = newCustom;
        }

        this.Direction = VERTICAL;
        this.ho.roc.rcChanged = true;
    }

    private void actSetLeftTop(CActExtension cActExtension) {
        this.PerspectiveDir = LEFTBOTTOM;
        this.ho.roc.rcChanged = true;
    }

    private void actSetRightBottom(CActExtension cActExtension) {
        this.PerspectiveDir = RIGHTTOP;
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustomValue(CActExtension cActExtension) {
        int index = cActExtension.getParamExpression(rh, 0);
        int value = cActExtension.getParamExpression(rh, 1);
        int objSize = Direction == HORIZONTAL ? ho.hoImgWidth : ho.hoImgHeight;

        if (index >= 0 && index < objSize) {
            CustomArray[index] = value;
        }

        ho.roc.rcChanged = true;
    }

    private void actSetWidth(CActExtension cActExtension) {
        this.ho.hoImgWidth = cActExtension.getParamExpression(this.rh, 0);
        resizePerspective(this.Direction == HORIZONTAL ? this.ho.hoImgWidth : this.ho.hoImgHeight);
    }

    private void actSetHeight(CActExtension cActExtension) {
        this.ho.hoImgHeight = cActExtension.getParamExpression(this.rh, 0);
        resizePerspective(this.Direction == HORIZONTAL ? this.ho.hoImgWidth : this.ho.hoImgHeight);
    }

    private void actSetResampleOn(CActExtension cActExtension) {
        this.resample = true;
        this.ho.roc.rcChanged = true;
    }

    private void actSetResampleOff(CActExtension cActExtension) {
        this.resample = false;
        this.ho.roc.rcChanged = true;
    }

    private void actSetSineOffset(CActExtension cActExtension) {
        this.Effect = SINEOFFSET;
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustomOffset(CActExtension cActExtension) {
        this.ho.roc.rcChanged = true;
    }

    private CValue expGetZoomValue() {
        this.expRet.forceInt(this.ZoomValue);
        return this.expRet;
    }

    private CValue expGetOffset() {
        this.expRet.forceInt(this.Offset);
        return this.expRet;
    }

    private CValue expNumWaves() {
        this.expRet.forceInt(this.SineWaveWaves);
        return this.expRet;
    }

    private CValue expGetCustom() {
        this.expRet.forceInt(this.CustomArray[Math.min(Math.max(0, this.ho.getExpParam().getInt()), (this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight) - 1)]);
        return this.expRet;
    }

    private CValue expGetWidth() {
        this.expRet.forceInt(this.ho.hoImgWidth);
        return this.expRet;
    }

    private CValue expGetHeight() {
        this.expRet.forceInt(this.ho.hoImgHeight);
        return this.expRet;
    }
}
