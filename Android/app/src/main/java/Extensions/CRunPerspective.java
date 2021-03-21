package Extensions;

import Actions.CActExtension;
import Banks.CImage;
import Conditions.CCndExtension;
import Expressions.CValue;
import Frame.CLayer;
import Objects.CExtension;
import Objects.CObject;
import OpenGL.GLRenderer;
import RunLoop.CCreateObjectInfo;
import Runtime.MMFRuntime;
import Runtime.SurfaceView;
import Services.CBinaryFile;
import Services.CServices;
import Sprites.CRSpr;

import android.graphics.Bitmap;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CRunPerspective extends CRunExtension {
    public static final int ACTADDCOORDINATES = 22;
    public static final int ACTADDOBJECT = 21;
    public static final int ACTAPPLYTO = 26;
    public static final int ACTDOTRANSFORM = 25;
    public static final int ACTPAUSEBACKGROUND = 18;
    public static final int ACTREMOVESLOT = 23;
    public static final int ACTRESETOBJECT = 24;
    public static final int ACTRESUMEBACKGROUND = 19;
    public static final int ACTSETCUSTOM = 4;
    public static final int ACTSETCUSTOMOFFSET = 17;
    public static final int ACTSETCUSTOMVALUE = 11;
    public static final int ACTSETHEIGHT = 13;
    public static final int ACTSETHORIZONTAL = 7;
    public static final int ACTSETLEFTTOP = 9;
    public static final int ACTSETNUMWAVES = 5;
    public static final int ACTSETOFFSET = 6;
    public static final int ACTSETPANORAMA = 1;
    public static final int ACTSETPERSPECTIVE = 2;
    public static final int ACTSETRESAMPLEOFF = 15;
    public static final int ACTSETRESAMPLEON = 14;
    public static final int ACTSETRIGHTBOTTOM = 10;
    public static final int ACTSETSINEOFFSET = 16;
    public static final int ACTSETSINEWAVE = 3;
    public static final int ACTSETVERTICAL = 8;
    public static final int ACTSETWIDTH = 12;
    public static final int ACTSETZOOMVALUE = 0;
    public static final int ACTUSEOBJECT = 20;

    public static final int CNDONTRANSFAVAILABLE = 0;
    public static final int CND_LAST = 1;

    public static final int EXPGETCUSTOM = 3;
    public static final int EXPGETHEIGHT = 5;
    public static final int EXPGETOFFSET = 1;
    public static final int EXPGETTHEIGHT = 9;
    public static final int EXPGETTPOSX = 6;
    public static final int EXPGETTPOSY = 7;
    public static final int EXPGETTSCALEX = 10;
    public static final int EXPGETTSCALEY = 11;
    public static final int EXPGETTWIDTH = 8;
    public static final int EXPGETWIDTH = 4;
    public static final int EXPGETZOOMVALUE = 0;
    public static final int EXPNUMWAVES = 2;

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

    private int[] CustomArray;
    private int Direction;
    private int Effect;
    private int Offset;
    private int PerspectiveDir;
    private int SineWaveWaves;
    private int ZoomValue;
    private boolean bRemoveShader;
    public CValue expRet = new CValue(0);
    private CImage imageTexture;
    private int indexShader;
    private CObject object;
    private boolean oldResample;
    private boolean onceOffs;
    private boolean oncePano;
    private boolean oncePers;
    private boolean onceSine;
    private boolean pauseReadingBckg;
    private boolean paused;
    private SparseArray<PosTransform> posTransform;
    private boolean resample;

    @Override // Extensions.CRunExtension
    public int getNumberOfConditions() {
        return 1;
    }

    @Override // Extensions.CRunExtension
    public int handleRunObject() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public class PosTransform {
        int Mode;
        float ObjAngle;
        int ObjBottom;
        int ObjLeft;
        int ObjRight;
        double ObjScaleX;
        double ObjScaleY;
        int ObjTop;
        int ObjX;
        int ObjY;
        int SlotNumber;
        CObject applyObj;
        boolean done;
        CObject obj;
        double posScaleX;
        double posScaleY;
        int posX;
        int posY;

        PosTransform() {
        }

        PosTransform(int i) {
            this.SlotNumber = i;
        }

        PosTransform(int i, CObject cObject) {
            this.SlotNumber = i;
            if (cObject != null) {
                this.Mode = 0;
                this.ObjX = cObject.hoX;
                this.ObjY = cObject.hoY;
                this.ObjLeft = cObject.hoX - cObject.hoImgXSpot;
                this.ObjTop = cObject.hoY - cObject.hoImgYSpot;
                this.ObjRight = this.ObjLeft + cObject.hoImgWidth;
                this.ObjBottom = this.ObjTop + cObject.hoImgHeight;
                this.ObjAngle = cObject.roc.rcAngle;
                this.ObjScaleX = (double) cObject.roc.rcScaleX;
                this.ObjScaleY = (double) cObject.roc.rcScaleY;
                this.posX = this.ObjX;
                this.posY = this.ObjY;
                this.obj = cObject;
            }
        }

        PosTransform(int i, int i2, int i3, int i4, int i5, int i6, int i7, float f, float f2, float f3, int i8) {
            this.SlotNumber = i;
            this.Mode = i8;
            this.ObjX = i2;
            this.ObjY = i3;
            this.ObjLeft = i4;
            this.ObjTop = i5;
            this.ObjRight = i6;
            this.ObjBottom = i7;
            this.ObjAngle = f3;
            this.ObjScaleX = (double) f;
            this.ObjScaleY = (double) f2;
            this.posX = i2;
            this.posY = i3;
            this.obj = null;
        }

        /* access modifiers changed from: package-private */
        public void resettingObj() {
            CObject cObject = this.applyObj;
            if (cObject != null) {
                synchronized (cObject) {
                    this.applyObj.hoX = this.ObjX;
                    this.applyObj.hoY = this.ObjY;
                    this.applyObj.roc.rcAngle = this.ObjAngle;
                    this.applyObj.setScale((float) this.ObjScaleX, (float) this.ObjScaleY, (this.applyObj.ros.rsFlags & 8) != 0);
                    this.applyObj.modif();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void updateTracking() {
            CObject cObject = this.obj;
            if (cObject != null) {
                this.ObjX = cObject.hoX;
                this.ObjY = this.obj.hoY;
                this.ObjLeft = this.obj.hoX - this.obj.hoImgXSpot;
                this.ObjTop = this.obj.hoY - this.obj.hoImgYSpot;
                this.ObjRight = this.ObjLeft + this.obj.hoImgWidth;
                this.ObjBottom = this.ObjTop + this.obj.hoImgHeight;
                this.ObjAngle = this.obj.roc.rcAngle;
                this.ObjScaleX = (double) this.obj.roc.rcScaleX;
                this.ObjScaleY = (double) this.obj.roc.rcScaleY;
                this.posX = this.ObjX;
                this.posY = this.ObjY;
            }
        }
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
        this.Direction = cBinaryFile.readByte() != 0 ? 1 : 0;
        cBinaryFile.skipBytes(2);
        this.ZoomValue = cBinaryFile.readInt();
        this.Offset = cBinaryFile.readInt();
        this.SineWaveWaves = cBinaryFile.readInt();
        this.PerspectiveDir = cBinaryFile.readByte() != 0 ? 0 : 1;
        this.resample = cBinaryFile.readByte() != 0;
        this.pauseReadingBckg = false;
        this.ho.roc.rcChanged = true;
        this.oldResample = (MMFRuntime.inst.app.hdr2Options & 4096) != 0;
        int i2 = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        this.CustomArray = new int[i2];
        for (int i3 = 0; i3 < i2; i3++) {
            this.CustomArray[i3] = this.ZoomValue;
        }
        this.imageTexture = new CImageTexture(this.ho.hoImgWidth, this.ho.hoImgHeight);
        this.paused = false;
        this.indexShader = -1;
        this.oncePers = false;
        this.oncePano = false;
        this.onceSine = false;
        this.onceOffs = false;
        this.bRemoveShader = false;
        this.posTransform = new SparseArray<>();
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
        CImage cImage = this.imageTexture;
        if (cImage != null) {
            cImage.destroy();
        }
        if (this.indexShader > 0 && GLRenderer.inst != null) {
            synchronized (GLRenderer.inst) {
                GLRenderer.inst.removeShader(this.indexShader);
            }
        }
        if (this.posTransform != null) {
            while (this.posTransform.size() > 0) {
                int keyAt = this.posTransform.keyAt(0);
                PosTransform posTransform2 = this.posTransform.get(keyAt);
                if (posTransform2 != null) {
                    if (!z) {
                        posTransform2.resettingObj();
                    }
                    this.posTransform.remove(keyAt);
                }
            }
            this.posTransform = null;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v10, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x023b A[Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x02ba }] */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x02f0 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x02f1  */
    @Override // Extensions.CRunExtension
    public void displayRunObject() {
        if (paused || ho.hoImgWidth == 0 || ho.hoImgHeight == 0) {
            return;
        }

        if (bRemoveShader && indexShader != -1) {
            bRemoveShader = false;
            GLRenderer.inst.removeShader(indexShader);
            indexShader = -1;
            oncePers = false;
            oncePano = false;
            onceSine = false;
            onceOffs = false;
        }

        if (indexShader == -1) {
            switch (Effect) {
                case PANORAMA:
                    indexShader = GLRenderer.inst.addShaderFromFile("panorama_ext", new String[]{"fB", "scale", "offset", "pDir"}, true, false);
                    break;
                case PERSPECTIVE:
                    indexShader = GLRenderer.inst.addShaderFromFile("perspective_ext", new String[]{"fA", "fB", "scale", "offset", "pDir"}, true, false);
                    break;
                case SINEWAVE:
                    indexShader = GLRenderer.inst.addShaderFromFile("sinewave_ext", new String[]{"Mode", "Zoom", "WaveIncrement", "Offset", "scale", "offset", "pDir"}, true, false);
                    break;
                case SINEOFFSET:
                    indexShader = GLRenderer.inst.addShaderFromFile("sineoffset_ext", new String[]{"Mode", "Zoom", "WaveIncrement", "Offset", "scale", "offset", "pDir"}, true, false);
                    break;
            }
        }

        if ((ho.ros.rsFlags & CRSpr.RSFLAG_VISIBLE) == 0 || pauseReadingBckg) {
            return;
        }

        CImage bgImage = null;

        int objX = ho.hoX - rh.rhWindowX;
        int objY = ho.hoY - rh.rhWindowY;
        int objWidth = ho.hoImgWidth;
        int objHeight = ho.hoImgHeight;

        float offsetX = 0.0f;
        float offsetY = 0.0f;
        float scaleY = 1.0f;
        float scaleX = 1.0f;

        //float WaveIncrement = (float)(SineWaveWaves * 360) / (float)objHeight;

        if (object == null) {
            GLRenderer.inst.readFrameToTexture(imageTexture, objX, objY, objWidth, objHeight);
            bgImage = imageTexture;
        }
        else if (object.hoType == 2) {
            bgImage = ho.hoAdRunHeader.rhApp.imageBank.getImageFromHandle(object.roc.rcImage);

            if (bgImage == null) {
                return;
            }

            int bgWidth = bgImage.getWidth();
            int bgHeight = bgImage.getHeight();

            scaleX = (float) objWidth / (float) bgWidth;
            scaleY = (float) objHeight / (float) bgHeight;
            offsetX = ((float) Math.abs(ho.hoX - object.hoX)) / (float) bgWidth;
            offsetY = ((float) Math.abs(ho.hoY - object.hoY)) / (float) bgHeight;

            if (scaleX > 1.0f || scaleY > 1.0f || offsetX < 0.0f || offsetY < 0.0f || objWidth > bgWidth || objHeight > bgHeight) {
                return;
            }
        }
        else if (object.hoType >= 32) {
            if (object instanceof CExtension) {
                CRunExtension extension = ((CExtension) object).ext;

                try {
                    if (Class.forName("Extensions.CRunkcpica").isInstance(extension)) {
                        Method method = extension.getClass().getMethod("getImage", null);

                        if (method == null) {
                            return;
                        }

                        bgImage = (CImage) method.invoke(extension, new Object[0]);
                    }
                } catch (Exception e) { }

                try {
                    if (Class.forName("Extensions.CRunKyso").isInstance(extension)) {
                        Field field = extension.getClass().getField("ho");

                        if (field == null) {
                            return;
                        }

                        bgImage = this.ho.getImageBank().getImageFromHandle(((CExtension) field.get(extension)).roc.rcImage);
                    }
                } catch (Exception e) {
                    return;
                }

                scaleX = (float) objWidth / (float) object.hoImgWidth;
                scaleY = (float) objHeight / (float) object.hoImgHeight;
                offsetX = Math.abs(ho.hoX - object.hoX) / (float) object.hoImgWidth;
                offsetY = Math.abs(ho.hoY - object.hoY) / (float) object.hoImgHeight;

                if (scaleX > 1.0f || scaleY > 1.0f || offsetX < 0.0f || offsetY < 0.0f || objWidth > object.hoImgWidth || objHeight > object.hoImgHeight) {
                    return;
                }
            }
        }

        if (bgImage == null) {
            return;
        }

        synchronized (bgImage) { synchronized (GLRenderer.inst) {
            GLRenderer.inst.pushClip(objX, objY, objWidth, objHeight);
            GLRenderer.inst.setEffectShader(indexShader);

            if (Effect == PANORAMA) {
                if (!oncePano && Direction == HORIZONTAL) {
                    //double sin = ((double) objHeight + (Math.sin(0.0d) * (double) ZoomValue)) - (double) ZoomValue;
                    //GLRenderer.inst.updateVariable1f("fB", ((float) Math.max(1.0d, sin)) / ((float) Math.max(1.0d, ((double) objHeight + (Math.sin(1.5707963267948966d) * (double) ZoomValue)) - (double) ZoomValue)));

                    GLRenderer.inst.updateVariable1f("fB", Math.max(1.0f, (float)(objHeight - ZoomValue)) / Math.max(1.0f, (float)objHeight));
                    GLRenderer.inst.updateVariable1i("pDir", 0);
                    oncePano = true;
                }
                else if (!oncePano && Direction == VERTICAL) {
                    //GLRenderer.inst.updateVariable1f("fB", ((float) Math.max(1.0, ((Math.sin(0.0d) * (double) ZoomValue) + (double) objWidth) - (double) ZoomValue)) / ((float) Math.max(1.0, ((double) objWidth + (Math.sin(1.5707963267948966d) * (double) ZoomValue)) - (double) ZoomValue)));

                    GLRenderer.inst.updateVariable1f("fB", Math.max(1.0f, (float)(objWidth - ZoomValue)) / Math.max(1.0f, (float)objWidth));
                    GLRenderer.inst.updateVariable1i("pDir", 1);
                    oncePano = true;
                }
            }
            else if (Effect == PERSPECTIVE) {
                if (!oncePers) {
                    double[] slope;

                    if (PerspectiveDir == LEFTBOTTOM) {
                        slope = LeftBottonSlope(ZoomValue, objWidth, objHeight, Direction);
                    } else {
                        slope = RightTopSlope(ZoomValue, objWidth, objHeight, Direction);
                    }

                    if (Direction == HORIZONTAL || object == null) {
                        GLRenderer.inst.updateVariable1f("fA", (float) slope[1]);
                        GLRenderer.inst.updateVariable1f("fB", (float) slope[0]);
                    } else {
                        GLRenderer.inst.updateVariable1f("fA", (float) slope[0]);
                        GLRenderer.inst.updateVariable1f("fB", (float) slope[1]);
                    }

                    GLRenderer.inst.updateVariable1i("pDir", Direction == HORIZONTAL ? 0 : 1);
                    oncePers = true;
                }
            }
            else if (Effect == SINEWAVE) {
                if (!onceSine && Direction == HORIZONTAL) {
                    GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / (float) objHeight);
                    //GLRenderer.inst.updateVariable1f("WaveIncrement", WaveIncrement * (float) objWidth);
                    GLRenderer.inst.updateVariable1f("WaveIncrement", ((float) (SineWaveWaves * 360) / (float) objHeight) * (float) objWidth);
                    GLRenderer.inst.updateVariable1f("Offset", (float) Offset);
                    GLRenderer.inst.updateVariable1i("pDir", 0);
                    onceSine = true;
                }
                else if(!onceSine && Direction == VERTICAL) {
                    GLRenderer.inst.setEffectShader(indexShader);
                    GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / ((float) objWidth));
                    //GLRenderer.inst.updateVariable1f("WaveIncrement", WaveIncrement * (float) objHeight);
                    GLRenderer.inst.updateVariable1f("WaveIncrement", (float) (SineWaveWaves * 360));
                    GLRenderer.inst.updateVariable1f("Offset", (float) Offset);
                    GLRenderer.inst.updateVariable1i("pDir", 1);

                    if (object == null) {
                        GLRenderer.inst.updateVariable1i("Mode", 0);
                    } else {
                        GLRenderer.inst.updateVariable1i("Mode", 1);
                    }

                    onceSine = true;
                }
            }
            else if (Effect == SINEOFFSET) {
                if (!onceOffs && Direction == HORIZONTAL) {
                    GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / (float) objHeight);
                    GLRenderer.inst.updateVariable1f("WaveIncrement", (float) (SineWaveWaves * 360));
                    GLRenderer.inst.updateVariable1f("Offset", (float) Offset);

                    if (object == null) {
                        GLRenderer.inst.updateVariable1i("Mode", 0);
                    } else {
                        GLRenderer.inst.updateVariable1i("Mode", 1);
                    }

                    GLRenderer.inst.updateVariable1i("pDir", 0);
                    onceOffs = true;
                }
                else if (!onceOffs && Direction == VERTICAL) {
                    GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / (float) objHeight);
                    GLRenderer.inst.updateVariable1f("WaveIncrement", ((float) (SineWaveWaves * 360) / (float) objHeight) * (float) objWidth);
                    GLRenderer.inst.updateVariable1f("Offset", (float) Offset);

                    if (object == null) {
                        GLRenderer.inst.updateVariable1i("Mode", 0);
                    } else {
                        GLRenderer.inst.updateVariable1i("Mode", 1);
                    }

                    GLRenderer.inst.updateVariable1i("pDir", 1);
                    onceOffs = true;
                }
            }

            GLRenderer.inst.updateVariable2f("scale", scaleX, scaleY);
            GLRenderer.inst.updateVariable2f("offset", offsetX, offsetY);
            bgImage.setResampling(resample);
            GLRenderer.inst.renderImage(bgImage, objX, objY, objWidth, objHeight, 0, 0);
            GLRenderer.inst.removeEffectShader();
            GLRenderer.inst.popClip();
        } }
    }

    /* access modifiers changed from: package-private */
    public double[] LeftBottonSlope(int zoom, int width, int height, int direction) {
        /*int i5;
        double[] dArr = {0.0d, 0.0d};
        int i6 = 0;
        if (direction == 0) {
            int i7 = 0;
            i5 = 0;
            while (i7 <= width) {
                double d = (double) height;
                dArr[i5] = ((d / (((double) (((i7 * zoom) / width) + height)) / d)) + 0.5d) / d;
                i7 += width;
                i5++;
            }
        } else {
            i5 = 0;
        }
        if (direction == 1) {
            while (i6 <= height) {
                double d2 = (double) width;
                dArr[i5] = ((d2 / (((double) (((i6 * zoom) / height) + width)) / d2)) + 0.5d) / d2;
                i6 += height;
                i5++;
            }
        }
        return dArr;*/
        double[] slope = { 0.0, 0.0 };

        switch (direction) {
            case HORIZONTAL:
                slope[0] = ((double) height + 0.5) / (double) height;
                slope[1] = (((double) height / ((double) (zoom + height) / (double) height)) + 0.5) / (double) height;
                break;
            case VERTICAL:
                slope[0] = ((double) width + 0.5) / (double) width;
                slope[1] = (((double) width / ((double) (zoom + width) / (double) width)) + 0.5) / (double) width;
                break;
        }

        return slope;
    }

    /* access modifiers changed from: package-private */
    public double[] RightTopSlope(int zoom, int width, int height, int direction) {
        /*int i5;
        double[] dArr = {0.0d, 0.0d};
        int i6 = 0;
        if (direction == 0) {
            int i7 = 0;
            i5 = 0;
            while (i7 <= width) {
                double d = height;
                dArr[i5] = ((d / (((double) ((((width - i7) * zoom) / width) + height)) / d)) + 0.5d) / d;
                i7 += width;
                i5++;
            }
        } else {
            i5 = 0;
        }
        if (direction == 1) {
            while (i6 <= height) {
                double d2 = width;
                dArr[i5] = ((d2 / (((double) ((((height - i6) * zoom) / height) + width)) / d2)) + 0.5d) / d2;
                i6 += height;
                i5++;
            }
        }
        return dArr;*/
        double[] slope = { 0.0, 0.0 };

        switch (direction) {
            case HORIZONTAL:
                slope[0] = (((double) height / ((double) (zoom + height) / (double) height)) + 0.5) / (double) height;
                slope[1] = ((double) height + 0.5) / (double) height;
                break;
            case VERTICAL:
                slope[0] = (((double) width / ((double) (zoom + width) / (double) width)) + 0.5) / (double) width;
                slope[0] = ((double) width + 0.5) / (double) width;
                break;
        }

        return slope;
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
        if (i != 0) {
            return false;
        }
        cndDoneTransformation(cCndExtension);
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
            case ACTPAUSEBACKGROUND:
                actPausePerspective(cActExtension);
                break;
            case ACTRESUMEBACKGROUND:
                actResumePerspective(cActExtension);
                break;
            case ACTUSEOBJECT:
                actUseObject(cActExtension);
                break;
            case ACTADDOBJECT:
                actAddObject(cActExtension);
                break;
            case ACTADDCOORDINATES:
                actAddCoordinates(cActExtension);
                break;
            case ACTREMOVESLOT:
                actRemoveSlot(cActExtension);
                break;
            case ACTRESETOBJECT:
                actResetObject(cActExtension);
                break;
            case ACTDOTRANSFORM:
                actDoTransform(cActExtension);
                break;
            case ACTAPPLYTO:
                actApplyTo(cActExtension);
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
            case EXPGETTPOSX:
                return expGetTPosX();
            case EXPGETTPOSY:
                return expGetTPosY();
            case EXPGETTWIDTH:
                return expGetTWidth();
            case EXPGETTHEIGHT:
                return expGetTHeight();
            case EXPGETTSCALEX:
                return expGetTScaleX();
            case EXPGETTSCALEY:
                return expGetTScaleY();
            default:
                return null;
        }
    }

    private boolean cndDoneTransformation(CCndExtension cCndExtension) {
        boolean z = false;
        int paramExpression = cCndExtension.getParamExpression(this.rh, 0);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (sparseArray == null || sparseArray.get(paramExpression) == null) {
            return false;
        }
        if (this.posTransform.get(paramExpression).SlotNumber == paramExpression) {
            z = true;
        }
        return this.posTransform.get(paramExpression).done & z;
    }

    private void actSetZoomValue(CActExtension cActExtension) {
        this.ZoomValue = cActExtension.getParamExpression(this.rh, 0);
        switch (Effect) {
            case PANORAMA:
                oncePano = false;
                break;
            case PERSPECTIVE:
                oncePers = false;
                break;
            case SINEWAVE:
                onceSine = false;
                break;
            case SINEOFFSET:
                onceOffs = false;
                break;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetPanorama(CActExtension cActExtension) {
        if (this.Effect != PANORAMA) {
            this.bRemoveShader = true;
        }
        this.Effect = PANORAMA;
        this.ho.roc.rcChanged = true;
    }

    private void actSetPerspective(CActExtension cActExtension) {
        if (this.Effect != PERSPECTIVE) {
            this.bRemoveShader = true;
        }
        this.Effect = PERSPECTIVE;
        this.ho.roc.rcChanged = true;
    }

    private void actSetSineWave(CActExtension cActExtension) {
        if (this.Effect != SINEWAVE) {
            this.bRemoveShader = true;
        }
        this.Effect = SINEWAVE;
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustom(CActExtension cActExtension) {
        this.ho.roc.rcChanged = true;
    }

    private void actSetNumWaves(CActExtension cActExtension) {
        this.SineWaveWaves = cActExtension.getParamExpression(this.rh, 0);
        this.onceSine = false;
        this.ho.roc.rcChanged = true;
    }

    private void actSetOffset(CActExtension cActExtension) {
        this.Offset = cActExtension.getParamExpression(this.rh, 0);
        switch (Effect) {
            case SINEWAVE:
                onceSine = false;
                break;
            case SINEOFFSET:
                onceOffs = false;
                break;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetHorizontal(CActExtension cActExtension) {
        int i = this.Direction == HORIZONTAL ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int i2 = this.ho.hoImgWidth;
        this.Direction = HORIZONTAL;
        this.ho.roc.rcChanged = true;
        int min = Math.min(i, i2);
        int[] iArr = new int[i2];
        if (min >= 0) System.arraycopy(this.CustomArray, 0, iArr, 0, min);
        this.CustomArray = iArr;
    }

    private void actSetVertical(CActExtension cActExtension) {
        int i = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int i2 = this.ho.hoImgHeight;
        this.Direction = 1;
        this.ho.roc.rcChanged = true;
        int min = Math.min(i, i2);
        int[] iArr = new int[i2];
        if (min >= 0) System.arraycopy(this.CustomArray, 0, iArr, 0, min);
        this.CustomArray = iArr;
    }

    private void actSetLeftTop(CActExtension cActExtension) {
        this.PerspectiveDir = LEFTBOTTOM;
        switch (Effect) {
            case PANORAMA:
                oncePano = false;
                break;
            case PERSPECTIVE:
                oncePers = false;
                break;
            case SINEWAVE:
                onceSine = false;
                break;
            case SINEOFFSET:
                onceOffs = false;
                break;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetRightBottom(CActExtension cActExtension) {
        this.PerspectiveDir = RIGHTTOP;
        switch (Effect) {
            case PANORAMA:
                oncePano = false;
                break;
            case PERSPECTIVE:
                oncePers = false;
                break;
            case SINEWAVE:
                onceSine = false;
                break;
            case SINEOFFSET:
                onceOffs = false;
                break;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustomValue(CActExtension cActExtension) {
        int paramExpression = cActExtension.getParamExpression(this.rh, 0);
        int paramExpression2 = cActExtension.getParamExpression(this.rh, 1);
        int i = this.Direction == HORIZONTAL ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        if (paramExpression >= 0 && paramExpression2 < i) {
            this.CustomArray[paramExpression] = paramExpression2;
        }
        this.ho.roc.rcChanged = true;
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
        CImage cImage;
        this.resample = true;
        if (!(this.oldResample || (cImage = this.imageTexture) == null)) {
            this.oldResample = true;
            cImage.setResampling(true);
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetResampleOff(CActExtension cActExtension) {
        CImage cImage;
        this.resample = false;
        if (this.oldResample && (cImage = this.imageTexture) != null) {
            this.oldResample = false;
            cImage.setResampling(false);
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetSineOffset(CActExtension cActExtension) {
        if (this.Effect != SINEOFFSET) {
            this.bRemoveShader = true;
        }
        this.Effect = SINEOFFSET;
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustomOffset(CActExtension cActExtension) {
        this.ho.roc.rcChanged = true;
    }

    private void actPausePerspective(CActExtension cActExtension) {
        this.pauseReadingBckg = true;
    }

    private void actResumePerspective(CActExtension cActExtension) {
        this.pauseReadingBckg = false;
        switch (Effect) {
            case PANORAMA:
                oncePano = false;
                break;
            case PERSPECTIVE:
                oncePers = false;
                break;
            case SINEWAVE:
                onceSine = false;
                break;
            case SINEOFFSET:
                onceOffs = false;
                break;
        }
        this.object = null;
    }

    private void actUseObject(CActExtension cActExtension) {
        this.object = cActExtension.getParamObject(this.rh, 0);
        switch (Effect) {
            case PANORAMA:
                oncePano = false;
                break;
            case PERSPECTIVE:
                oncePers = false;
                break;
            case SINEWAVE:
                onceSine = false;
                break;
            case SINEOFFSET:
                onceOffs = false;
                break;
        }
    }

    private void actAddObject(CActExtension cActExtension) {
        int paramExpression = cActExtension.getParamExpression(this.rh, 0);
        CObject paramObject = cActExtension.getParamObject(this.rh, 1);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (sparseArray != null) {
            if (sparseArray.get(paramExpression) != null && this.posTransform.get(paramExpression).SlotNumber == paramExpression) {
                this.posTransform.remove(paramExpression);
            }
            this.posTransform.put(paramExpression, new PosTransform(paramExpression, paramObject));
        }
    }

    private void actAddCoordinates(CActExtension cActExtension) {
        int paramExpression = cActExtension.getParamExpression(this.rh, 0);
        int paramExpression2 = cActExtension.getParamExpression(this.rh, 1);
        int paramExpression3 = cActExtension.getParamExpression(this.rh, 2);
        int paramExpression4 = cActExtension.getParamExpression(this.rh, 3);
        int paramExpression5 = cActExtension.getParamExpression(this.rh, 4);
        int paramExpression6 = cActExtension.getParamExpression(this.rh, 5);
        int paramExpression7 = cActExtension.getParamExpression(this.rh, 6);
        float paramExpFloat = cActExtension.getParamExpFloat(this.rh, 7);
        float paramExpFloat2 = cActExtension.getParamExpFloat(this.rh, 8);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (sparseArray != null) {
            if (sparseArray.get(paramExpression) != null && this.posTransform.get(paramExpression).SlotNumber == paramExpression) {
                this.posTransform.remove(paramExpression);
            }
            this.posTransform.put(paramExpression, new PosTransform(paramExpression, paramExpression2, paramExpression3, paramExpression4, paramExpression5, paramExpression6, paramExpression7, paramExpFloat, paramExpFloat2, 0.0f, 1));
        }
    }

    private void actRemoveSlot(CActExtension cActExtension) {
        int paramExpression = cActExtension.getParamExpression(this.rh, 0);
        if (this.posTransform != null) {
            for (int i = 0; i < this.posTransform.size(); i++) {
                if (this.posTransform.get(i) != null) {
                    int keyAt = this.posTransform.keyAt(i);
                    if (this.posTransform.get(keyAt).SlotNumber == paramExpression) {
                        this.posTransform.remove(keyAt);
                    }
                }
            }
        }
    }

    private void actResetObject(CActExtension cActExtension) {
        cActExtension.getParamExpression(this.rh, 0);
        if (this.posTransform != null) {
            while (this.posTransform.size() > 0) {
                int keyAt = this.posTransform.keyAt(0);
                if (this.posTransform.get(keyAt) != null) {
                    this.posTransform.remove(keyAt);
                }
            }
            this.posTransform.clear();
        }
    }

    private void actDoTransform(CActExtension cActExtension) {
        int paramExpression = cActExtension.getParamExpression(this.rh, 0);
        int paramExpression2 = cActExtension.getParamExpression(this.rh, 1);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (sparseArray != null && sparseArray.get(paramExpression) != null && this.posTransform.get(paramExpression).SlotNumber == paramExpression) {
            this.posTransform.get(paramExpression).Mode = paramExpression2;
            DoCalculation(this.posTransform.get(paramExpression), null);
        }
    }

    private void actApplyTo(CActExtension cActExtension) {
        int paramExpression = cActExtension.getParamExpression(this.rh, 0);
        CObject paramObject = cActExtension.getParamObject(this.rh, 1);
        int paramExpression2 = cActExtension.getParamExpression(this.rh, 2);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (sparseArray != null && sparseArray.get(paramExpression) != null && this.posTransform.get(paramExpression).SlotNumber == paramExpression) {
            this.posTransform.get(paramExpression).Mode = paramExpression2;
            DoCalculation(this.posTransform.get(paramExpression), paramObject);
        }
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

    private CValue expGetTPosX() {
        int i = this.ho.getExpParam().getInt();
        this.expRet.forceInt(0);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (!(sparseArray == null || sparseArray.get(i) == null)) {
            this.expRet.forceInt(this.posTransform.get(i).posX);
        }
        return this.expRet;
    }

    private CValue expGetTPosY() {
        int i = this.ho.getExpParam().getInt();
        this.expRet.forceInt(0);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (!(sparseArray == null || sparseArray.get(i) == null)) {
            this.expRet.forceInt(this.posTransform.get(i).posY);
        }
        return this.expRet;
    }

    private CValue expGetTWidth() {
        int i = this.ho.getExpParam().getInt();
        this.expRet.forceInt(0);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (!(sparseArray == null || sparseArray.get(i) == null)) {
            PosTransform posTransform2 = this.posTransform.get(i);
            double d = posTransform2.posScaleX;
            this.expRet.forceInt((int) (((double) (posTransform2.ObjRight - posTransform2.ObjLeft)) * d));
        }
        return this.expRet;
    }

    private CValue expGetTHeight() {
        int i = this.ho.getExpParam().getInt();
        this.expRet.forceInt(0);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (!(sparseArray == null || sparseArray.get(i) == null)) {
            PosTransform posTransform2 = this.posTransform.get(i);
            double d = posTransform2.posScaleY;
            this.expRet.forceInt((int) (((double) (posTransform2.ObjBottom - posTransform2.ObjTop)) * d));
        }
        return this.expRet;
    }

    private CValue expGetTScaleX() {
        int i = this.ho.getExpParam().getInt();
        this.expRet.forceDouble(1.0d);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (!(sparseArray == null || sparseArray.get(i) == null)) {
            this.expRet.forceDouble(this.posTransform.get(i).posScaleX * this.posTransform.get(i).ObjScaleX);
        }
        return this.expRet;
    }

    private CValue expGetTScaleY() {
        int i = this.ho.getExpParam().getInt();
        this.expRet.forceDouble(1.0d);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (!(sparseArray == null || sparseArray.get(i) == null)) {
            this.expRet.forceDouble(this.posTransform.get(i).posScaleY * this.posTransform.get(i).ObjScaleY);
        }
        return this.expRet;
    }

    private void DoCalculation(PosTransform posTransform2, CObject cObject) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8 = this.ho.hoImgWidth;
        int i9 = this.ho.hoImgHeight;
        CLayer cLayer = this.rh.rhFrame.layers[this.ho.hoLayer];
        int i10 = this.ho.hoX;
        int i11 = this.rh.rhWindowX;
        int i12 = this.ho.hoY;
        int i13 = this.rh.rhWindowY;
        int i14 = this.ZoomValue;
        if (posTransform2 != null) {
            posTransform2.applyObj = cObject;
            posTransform2.posScaleX = 1.0d;
            posTransform2.posScaleY = 1.0d;
            if (posTransform2.obj != null) {
                posTransform2.updateTracking();
            }
            if (!this.paused && (this.ho.ros.rsFlags & 32) != 0 && (cLayer.dwOptions & 16) != 0) {
                if (this.Effect == 0 && this.Direction == 0) {
                    double d = i9;
                    double d2 = i14;
                    double max = (((double) (((float) Math.max(1.0d, (d + (Math.sin(0.0d) * d2)) - d2)) / ((float) Math.max(1.0d, (d + (Math.sin(1.5707963267948966d) * d2)) - d2)))) - 1.0d) * 4.0d;
                    float f = (float) i8;
                    i2 = i14;
                    double max2 = 1.0d / Math.max(0.02d, (Math.pow((double) ((((float) (posTransform2.ObjLeft - this.ho.hoX)) / f) - 0.5f), 2.0d) * max) + 1.0d);
                    i = i8;
                    double max3 = 1.0d / Math.max(0.02d, (Math.pow((double) ((((float) (posTransform2.ObjRight - this.ho.hoX)) / f) - 0.5f), 2.0d) * max) + 1.0d);
                    int i15 = posTransform2.Mode;
                    if (i15 != 0) {
                        if (i15 != 1) {
                            if (i15 == 2) {
                                posTransform2.posScaleY = (max2 + max3) / 2.0d;
                            }
                        } else if (max2 > max3) {
                            posTransform2.posScaleY = max2;
                        } else {
                            posTransform2.posScaleY = max3;
                        }
                    } else if (max2 < max3) {
                        posTransform2.posScaleY = max2;
                    } else {
                        posTransform2.posScaleY = max3;
                    }
                    float max4 = (float) (1.0d / Math.max(0.02d, (max * Math.pow((double) ((((float) (posTransform2.ObjX - this.ho.hoX)) / f) - 0.5f), 2.0d)) + 1.0d));
                    int i16 = posTransform2.ObjX;
                    int i17 = posTransform2.ObjY - this.ho.hoY;
                    int i18 = i9 / 2;
                    posTransform2.posX = i16;
                    posTransform2.posY = ((int) (max4 * ((float) (i17 - i18)))) + i18;
                } else {
                    i = i8;
                    i2 = i14;
                }
                if (this.Effect == 0 && this.Direction == 1) {
                    i3 = i;
                    double d3 = i3;
                    double d4 = i2;
                    double max5 = (((double) (((float) Math.max(1.0d, ((Math.sin(0.0d) * d4) + d3) - d4)) / ((float) Math.max(1.0d, (d3 + (Math.sin(1.5707963267948966d) * d4)) - d4)))) - 1.0d) * 4.0d;
                    float f2 = (float) i9;
                    double max6 = 1.0d / Math.max(0.05d, (Math.pow((double) ((((float) (posTransform2.ObjTop - this.ho.hoY)) / f2) - 0.5f), 2.0d) * max5) + 1.0d);
                    double max7 = 1.0d / Math.max(0.05d, (Math.pow((double) ((((float) (posTransform2.ObjBottom - this.ho.hoY)) / f2) - 0.5f), 2.0d) * max5) + 1.0d);
                    int i19 = posTransform2.Mode;
                    if (i19 != 0) {
                        if (i19 != 1) {
                            if (i19 == 2) {
                                posTransform2.posScaleX = (max6 + max7) / 2.0d;
                            }
                        } else if (max6 > max7) {
                            posTransform2.posScaleX = max6;
                        } else {
                            posTransform2.posScaleX = max7;
                        }
                    } else if (max6 < max7) {
                        posTransform2.posScaleX = max6;
                    } else {
                        posTransform2.posScaleX = max7;
                    }
                    float max8 = (float) (1.0d / Math.max(0.05d, (max5 * Math.pow((double) ((((float) (posTransform2.ObjY - this.ho.hoY)) / f2) - 0.5f), 2.0d)) + 1.0d));
                    int i20 = posTransform2.ObjY;
                    int i21 = i3 / 2;
                    posTransform2.posX = ((int) (max8 * ((float) ((posTransform2.ObjX - this.ho.hoX) - i21)))) + i21;
                    posTransform2.posY = i20;
                } else {
                    i3 = i;
                }
                if (this.Effect == 1 && (i7 = this.Direction) == 0 && this.PerspectiveDir == 0) {
                    double[] LeftBottonSlope = LeftBottonSlope(i2, i3, i9, i7);
                    float f3 = (float) LeftBottonSlope[0];
                    double d5 = f3;
                    double d6 = (double) (f3 - ((float) LeftBottonSlope[1]));
                    float f4 = (float) i3;
                    double d7 = 1.0d / (d5 - ((1.0d - ((double) (((float) (posTransform2.ObjLeft - this.ho.hoX)) / f4))) * d6));
                    double d8 = 1.0d / (d5 - ((1.0d - ((double) (((float) (posTransform2.ObjRight - this.ho.hoX)) / f4))) * d6));
                    int i22 = posTransform2.Mode;
                    if (i22 != 0) {
                        if (i22 != 1) {
                            if (i22 == 2) {
                                posTransform2.posScaleY = (d7 + d8) / 2.0d;
                            }
                        } else if (d7 > d8) {
                            posTransform2.posScaleY = d7;
                        } else {
                            posTransform2.posScaleY = d8;
                        }
                    } else if (d7 < d8) {
                        posTransform2.posScaleY = d7;
                    } else {
                        posTransform2.posScaleY = d8;
                    }
                    int i23 = posTransform2.ObjX;
                    double d9 = (double) ((posTransform2.ObjY - this.ho.hoY) - (i9 / 2));
                    posTransform2.posX = i23;
                    posTransform2.posY = (int) ((d9 * (1.0d / (d5 - (d6 * (1.0d - (((double) (posTransform2.ObjX - this.ho.hoX)) / ((double) i3))))))) + (((double) i9) / 2.0d));
                }
                if (this.Effect == 1 && (i6 = this.Direction) == 1 && this.PerspectiveDir == 0) {
                    double[] LeftBottonSlope2 = LeftBottonSlope(i2, i3, i9, i6);
                    float f5 = (float) LeftBottonSlope2[0];
                    double d10 = (double) f5;
                    double d11 = (double) (f5 - ((float) LeftBottonSlope2[1]));
                    float f6 = (float) i9;
                    double d12 = 1.0d / (d10 - ((1.0d - ((double) (((float) (posTransform2.ObjTop - this.ho.hoY)) / f6))) * d11));
                    double d13 = 1.0d / (d10 - ((1.0d - ((double) (((float) (posTransform2.ObjBottom - this.ho.hoY)) / f6))) * d11));
                    int i24 = posTransform2.Mode;
                    if (i24 != 0) {
                        if (i24 != 1) {
                            if (i24 == 2) {
                                posTransform2.posScaleX = (d12 + d13) / 2.0d;
                            }
                        } else if (d12 > d13) {
                            posTransform2.posScaleX = d12;
                        } else {
                            posTransform2.posScaleX = d13;
                        }
                    } else if (d12 < d13) {
                        posTransform2.posScaleX = d12;
                    } else {
                        posTransform2.posScaleX = d13;
                    }
                    int i25 = (int) ((((double) ((posTransform2.obj.hoX - this.ho.hoX) - (i3 / 2))) * (1.0d / (d10 - (d11 * (1.0d - (((double) (posTransform2.ObjY - this.ho.hoY)) / ((double) i9))))))) + (((double) i3) / 2.0d));
                    int i26 = posTransform2.ObjY;
                    posTransform2.posX = i25;
                    posTransform2.posY = i26;
                }
                if (this.Effect == 1 && (i5 = this.Direction) == 0 && this.PerspectiveDir == 1) {
                    double[] RightTopSlope = RightTopSlope(i2, i3, i9, i5);
                    float f7 = (float) RightTopSlope[0];
                    double d14 = (double) f7;
                    double d15 = (double) (f7 - ((float) RightTopSlope[1]));
                    float f8 = (float) i3;
                    double d16 = 1.0d / (d14 - ((1.0d - ((double) (((float) (posTransform2.ObjLeft - this.ho.hoX)) / f8))) * d15));
                    double d17 = 1.0d / (d14 - ((1.0d - ((double) (((float) (posTransform2.ObjRight - this.ho.hoX)) / f8))) * d15));
                    int i27 = posTransform2.Mode;
                    if (i27 != 0) {
                        if (i27 != 1) {
                            if (i27 == 2) {
                                posTransform2.posScaleY = (d16 + d17) / 2.0d;
                            }
                        } else if (d16 > d17) {
                            posTransform2.posScaleY = d16;
                        } else {
                            posTransform2.posScaleY = d17;
                        }
                    } else if (d16 < d17) {
                        posTransform2.posScaleY = d16;
                    } else {
                        posTransform2.posScaleY = d17;
                    }
                    int i28 = posTransform2.ObjX;
                    double d18 = (double) ((posTransform2.obj.hoY - this.ho.hoY) - (i9 / 2));
                    posTransform2.posX = i28;
                    posTransform2.posY = (int) ((d18 * (1.0d / (d14 - (d15 * (1.0d - (((double) (posTransform2.ObjX - this.ho.hoX)) / ((double) i3))))))) + (((double) i9) / 2.0d));
                }
                if (this.Effect == 1 && (i4 = this.Direction) == 1 && this.PerspectiveDir == 1) {
                    double[] RightTopSlope2 = RightTopSlope(i2, i3, i9, i4);
                    float f9 = (float) RightTopSlope2[0];
                    double d19 = (double) f9;
                    double d20 = (double) (f9 - ((float) RightTopSlope2[1]));
                    float f10 = (float) i9;
                    double d21 = 1.0d / (d19 - ((1.0d - ((double) (((float) (posTransform2.ObjTop - this.ho.hoY)) / f10))) * d20));
                    double d22 = 1.0d / (d19 - ((1.0d - ((double) (((float) (posTransform2.ObjBottom - this.ho.hoY)) / f10))) * d20));
                    int i29 = posTransform2.Mode;
                    if (i29 != 0) {
                        if (i29 != 1) {
                            if (i29 == 2) {
                                posTransform2.posScaleX = (d21 + d22) / 2.0d;
                            }
                        } else if (d21 > d22) {
                            posTransform2.posScaleX = d21;
                        } else {
                            posTransform2.posScaleX = d22;
                        }
                    } else if (d21 < d22) {
                        posTransform2.posScaleX = d21;
                    } else {
                        posTransform2.posScaleX = d22;
                    }
                    int i30 = (int) ((((double) ((posTransform2.obj.hoX - this.ho.hoX) - (i3 / 2))) * (1.0d / (d19 - (d20 * (1.0d - (((double) (posTransform2.ObjY - this.ho.hoY)) / ((double) i9))))))) + (((double) i3) / 2.0d));
                    int i31 = posTransform2.ObjY;
                    posTransform2.posX = i30;
                    posTransform2.posY = i31;
                }
            } else if (posTransform2.obj != null) {
                posTransform2.posX = posTransform2.obj.hoX;
                posTransform2.posY = posTransform2.obj.hoY;
            } else {
                posTransform2.posX = posTransform2.ObjX;
                posTransform2.posY = posTransform2.ObjY;
            }
            if (cObject != null) {
                cObject.hoX = posTransform2.posX;
                cObject.hoY = posTransform2.posY;
                cObject.roc.rcAngle = posTransform2.ObjAngle;
                cObject.setScale((float) (posTransform2.posScaleX * posTransform2.ObjScaleX), (float) (posTransform2.posScaleY * posTransform2.ObjScaleY), (cObject.ros.rsFlags & 8) != 0);
                cObject.modif();
            }
            posTransform2.done = true;
        }
    }
}
