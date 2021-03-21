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
import android.graphics.Bitmap;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    public static final int CUSTOM = 4;
    public static final int CUSTOMOFFSET = 5;
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
    public static final int HORIZONTAL = 0;
    public static final int LEFTBOTTOM = 0;
    public static final int PANORAMA = 0;
    public static final int PERSPECTIVE = 1;
    public static final int RIGHTTOP = 1;
    public static final int SINEOFFSET = 3;
    public static final int SINEWAVE = 2;
    public static final int VERTICAL = 1;
    public static final float delta = 0.017453289f;
    public static final int[] offsetRange = {-16383, 16383};
    public static final int[] waveRange = {0, 32767};
    public static final int[] zoomRange = {0, 32767};
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
    private int oper_slot;
    private int orientation;
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
            allocNative2((MMFRuntime.inst.app.hdr2Options & 4096) != 0, -1, CServices.getBitmapPixels(createBitmap), 0, 0, 0, 0, createBitmap.getWidth(), createBitmap.getHeight(), SurfaceView.ES);
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
        int i;
        float f;
        int i2;
        float f2;
        int i3;
        CImage cImage;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        float f8;
        float f9;
        double d;
        float f10;
        int i9;
        int i10;
        float f11;
        float f12;
        float f13;
        float f14;
        int i11;
        float f15;
        float f16;
        float f17;
        float f18;
        float f19;
        float f20;
        float f21;
        float f22;
        float f23;
        float f24;
        int i12;
        float f25;
        float f26;
        float f27;
        float f28;
        int i13;
        float f29;
        float f30;
        int i14;
        int i15;
        boolean z;
        int i16;
        boolean z2;
        boolean z3;
        boolean z4;
        boolean z5;
        int i17;
        int i18;
        int i19;
        int i20;
        float f31;
        CImage cImage2;
        float f32;
        float f33;
        float f34;
        float f35;
        CImage cImage3;
        float f36;
        float f37;
        float f38;
        int i21;
        int i22;
        float f39;
        float f40;
        float f41;
        int i23;
        GLRenderer gLRenderer = GLRenderer.inst;
        if (!this.paused && this.ho.hoImgWidth != 0 && this.ho.hoImgHeight != 0) {
            if (this.bRemoveShader && (i23 = this.indexShader) != -1) {
                this.bRemoveShader = false;
                gLRenderer.removeShader(i23);
                this.indexShader = -1;
                this.oncePers = false;
                this.oncePano = false;
                this.onceSine = false;
                this.onceOffs = false;
            }
            if (this.indexShader == -1) {
                if (this.Effect == 0) {
                    this.indexShader = GLRenderer.inst.addShaderFromFile("panorama_ext", new String[]{"fB", "scale", "offset", "pDir"}, true, false);
                }
                if (this.Effect == 1) {
                    this.indexShader = GLRenderer.inst.addShaderFromFile("perspective_ext", new String[]{"fA", "fB", "scale", "offset", "pDir"}, true, false);
                }
                if (this.Effect == 2) {
                    this.indexShader = GLRenderer.inst.addShaderFromFile("sinewave_ext", new String[]{"Mode", "Zoom", "WaveIncrement", "Offset", "scale", "offset", "pDir"}, true, false);
                }
                if (this.Effect == 3) {
                    this.indexShader = GLRenderer.inst.addShaderFromFile("sineoffset_ext", new String[]{"Mode", "Zoom", "WaveIncrement", "Offset", "scale", "offset", "pDir"}, true, false);
                }
            }
            if ((this.ho.ros.rsFlags & 32) != 0) {
                int i24 = this.ho.hoImgWidth;
                int i25 = this.ho.hoImgHeight;
                CLayer cLayer = this.rh.rhFrame.layers[this.ho.hoLayer];
                int i26 = this.ho.hoX - this.rh.rhWindowX;
                int i27 = this.ho.hoY - this.rh.rhWindowY;
                int i28 = this.Effect;
                int i29 = this.ZoomValue;
                int i30 = this.Offset;
                float f42 = (float) i25;
                float f43 = ((float) (this.SineWaveWaves * 360)) / f42;
                if (!this.pauseReadingBckg) {
                    CObject cObject = this.object;
                    if (cObject == null) {
                        f = f42;
                        i = i30;
                        i3 = i29;
                        i2 = i28;
                        gLRenderer.readFrameToTexture(this.imageTexture, i26, i27, i24, i25);
                        cImage2 = this.imageTexture;
                    } else {
                        f = f42;
                        i = i30;
                        i3 = i29;
                        i2 = i28;
                        if (cObject.hoType == 2) {
                            cImage3 = this.ho.hoAdRunHeader.rhApp.imageBank.getImageFromHandle(this.object.roc.rcImage);
                            if (cImage3 != null) {
                                int width = cImage3.getWidth();
                                int height = cImage3.getHeight();
                                float f44 = (float) width;
                                f33 = ((float) i24) / f44;
                                float f45 = (float) height;
                                f32 = f / f45;
                                f35 = ((float) Math.abs(this.ho.hoX - this.object.hoX)) / f44;
                                f34 = ((float) Math.abs(this.ho.hoY - this.object.hoY)) / f45;
                                if (f33 > 1.0f || f32 > 1.0f || f35 < 0.0f || f34 < 0.0f || i24 > width || i25 > height) {
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            cImage3 = null;
                            f35 = 0.0f;
                            f34 = 0.0f;
                            f33 = 1.0f;
                            f32 = 1.0f;
                        }
                        if (this.object.hoType >= 32) {
                            CObject cObject2 = this.object;
                            if (cObject2 instanceof CExtension) {
                                CRunExtension cRunExtension = ((CExtension) cObject2).ext;
                                if (cRunExtension instanceof CRunExtension) {
                                    try {
                                        if (Class.forName("Extensions.CRunkcpica").isInstance(cRunExtension)) {
                                            Method method = cRunExtension.getClass().getMethod("getImage", null);
                                            if (method != null) {
                                                try {
                                                    cImage3 = (CImage) method.invoke(cRunExtension, new Object[0]);
                                                } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
                                                    f38 = f34;
                                                    f34 = f38;
                                                    if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                    }
                                                    f37 = f35;
                                                    f36 = f33;
                                                    cImage = cImage3;
                                                    f3 = f34;
                                                    f2 = f37;
                                                    f4 = f36;
                                                    f5 = f32;
                                                    if (cImage == null) {
                                                    }
                                                }
                                            }
                                            try {
                                                i21 = this.object.hoImgWidth;
                                                i22 = this.object.hoImgHeight;
                                                f39 = (float) i21;
                                                f40 = ((float) i24) / f39;
                                                f41 = (float) i22;
                                                f32 = f / f41;
                                            } catch (ClassNotFoundException unused2) {
                                                f38 = f34;
                                                f34 = f38;
                                                if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                }
                                                f37 = f35;
                                                f36 = f33;
                                                cImage = cImage3;
                                                f3 = f34;
                                                f2 = f37;
                                                f4 = f36;
                                                f5 = f32;
                                                if (cImage == null) {
                                                }
                                            } catch (NoSuchMethodException unused3) {
                                                f38 = f34;
                                                f34 = f38;
                                                if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                }
                                                f37 = f35;
                                                f36 = f33;
                                                cImage = cImage3;
                                                f3 = f34;
                                                f2 = f37;
                                                f4 = f36;
                                                f5 = f32;
                                                if (cImage == null) {
                                                }
                                            } catch (IllegalAccessException unused4) {
                                                f38 = f34;
                                                f34 = f38;
                                                if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                }
                                                f37 = f35;
                                                f36 = f33;
                                                cImage = cImage3;
                                                f3 = f34;
                                                f2 = f37;
                                                f4 = f36;
                                                f5 = f32;
                                                if (cImage == null) {
                                                }
                                            } catch (InvocationTargetException unused5) {
                                                f38 = f34;
                                                f34 = f38;
                                                if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                }
                                                f37 = f35;
                                                f36 = f33;
                                                cImage = cImage3;
                                                f3 = f34;
                                                f2 = f37;
                                                f4 = f36;
                                                f5 = f32;
                                                if (cImage == null) {
                                                }
                                            }
                                            try {
                                                f38 = f34;
                                                try {
                                                    f35 = ((float) Math.abs(this.ho.hoX - this.object.hoX)) / f39;
                                                } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused6) {
                                                    f33 = f40;
                                                    cImage3 = cImage3;
                                                    f35 = f35;
                                                    f34 = f38;
                                                    if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                    }
                                                    f37 = f35;
                                                    f36 = f33;
                                                    cImage = cImage3;
                                                    f3 = f34;
                                                    f2 = f37;
                                                    f4 = f36;
                                                    f5 = f32;
                                                    if (cImage == null) {
                                                    }
                                                }
                                                try {
                                                    f34 = ((float) Math.abs(this.ho.hoY - this.object.hoY)) / f41;
                                                    if (f40 <= 1.0f && f32 <= 1.0f && f35 >= 0.0f && f34 >= 0.0f && i24 <= i21 && i25 <= i22) {
                                                        f33 = f40;
                                                        cImage3 = cImage3;
                                                    } else {
                                                        return;
                                                    }
                                                } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused7) {
                                                    f33 = f40;
                                                    cImage3 = cImage3;
                                                    f34 = f38;
                                                    if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                    }
                                                    f37 = f35;
                                                    f36 = f33;
                                                    cImage = cImage3;
                                                    f3 = f34;
                                                    f2 = f37;
                                                    f4 = f36;
                                                    f5 = f32;
                                                    if (cImage == null) {
                                                    }
                                                }
                                            } catch (ClassNotFoundException unused8) {
                                                f38 = f34;
                                                f33 = f40;
                                                cImage3 = cImage3;
                                                f35 = f35;
                                                f34 = f38;
                                                if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                }
                                                f37 = f35;
                                                f36 = f33;
                                                cImage = cImage3;
                                                f3 = f34;
                                                f2 = f37;
                                                f4 = f36;
                                                f5 = f32;
                                                if (cImage == null) {
                                                }
                                            } catch (NoSuchMethodException unused9) {
                                                f38 = f34;
                                                f33 = f40;
                                                cImage3 = cImage3;
                                                f35 = f35;
                                                f34 = f38;
                                                if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                }
                                                f37 = f35;
                                                f36 = f33;
                                                cImage = cImage3;
                                                f3 = f34;
                                                f2 = f37;
                                                f4 = f36;
                                                f5 = f32;
                                                if (cImage == null) {
                                                }
                                            } catch (IllegalAccessException unused10) {
                                                f38 = f34;
                                                f33 = f40;
                                                cImage3 = cImage3;
                                                f35 = f35;
                                                f34 = f38;
                                                if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                }
                                                f37 = f35;
                                                f36 = f33;
                                                cImage = cImage3;
                                                f3 = f34;
                                                f2 = f37;
                                                f4 = f36;
                                                f5 = f32;
                                                if (cImage == null) {
                                                }
                                            } catch (InvocationTargetException unused11) {
                                                f38 = f34;
                                                f33 = f40;
                                                cImage3 = cImage3;
                                                f35 = f35;
                                                f34 = f38;
                                                if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                                }
                                                f37 = f35;
                                                f36 = f33;
                                                cImage = cImage3;
                                                f3 = f34;
                                                f2 = f37;
                                                f4 = f36;
                                                f5 = f32;
                                                if (cImage == null) {
                                                }
                                            }
                                        }
                                    } catch (ClassNotFoundException unused12) {
                                        f38 = f34;
                                        f34 = f38;
                                        if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                        }
                                        f37 = f35;
                                        f36 = f33;
                                        cImage = cImage3;
                                        f3 = f34;
                                        f2 = f37;
                                        f4 = f36;
                                        f5 = f32;
                                        if (cImage == null) {
                                        }
                                    } catch (NoSuchMethodException unused13) {
                                        f38 = f34;
                                        f34 = f38;
                                        if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                        }
                                        f37 = f35;
                                        f36 = f33;
                                        cImage = cImage3;
                                        f3 = f34;
                                        f2 = f37;
                                        f4 = f36;
                                        f5 = f32;
                                        if (cImage == null) {
                                        }
                                    } catch (IllegalAccessException unused14) {
                                        f38 = f34;
                                        f34 = f38;
                                        if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                        }
                                        f37 = f35;
                                        f36 = f33;
                                        cImage = cImage3;
                                        f3 = f34;
                                        f2 = f37;
                                        f4 = f36;
                                        f5 = f32;
                                        if (cImage == null) {
                                        }
                                    } catch (InvocationTargetException unused15) {
                                        f38 = f34;
                                        f34 = f38;
                                        if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                        }
                                        f37 = f35;
                                        f36 = f33;
                                        cImage = cImage3;
                                        f3 = f34;
                                        f2 = f37;
                                        f4 = f36;
                                        f5 = f32;
                                        if (cImage == null) {
                                        }
                                    }
                                    try {
                                        if (Class.forName("Extensions.CRunKyso").isInstance(cRunExtension)) {
                                            Field field = cRunExtension.getClass().getField("ho");
                                            if (field != null) {
                                                try {
                                                    cImage3 = this.ho.getImageBank().getImageFromHandle(((CExtension) field.get(cRunExtension)).roc.rcImage);
                                                } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException unused16) {
                                                }
                                            }
                                            try {
                                                int i31 = this.object.hoImgWidth;
                                                int i32 = this.object.hoImgHeight;
                                                float f46 = (float) i31;
                                                f33 = ((float) i24) / f46;
                                                float f47 = (float) i32;
                                                f32 = f / f47;
                                                try {
                                                    try {
                                                        f35 = ((float) Math.abs(this.ho.hoX - this.object.hoX)) / f46;
                                                        try {
                                                            f34 = ((float) Math.abs(this.ho.hoY - this.object.hoY)) / f47;
                                                            if (f33 > 1.0f || f32 > 1.0f || f35 < 0.0f || f34 < 0.0f || i24 > i31 || i25 > i32) {
                                                                return;
                                                            }
                                                        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException unused17) {
                                                        }
                                                        cImage3 = cImage3;
                                                    } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException unused18) {
                                                        cImage3 = cImage3;
                                                        f35 = f35;
                                                    }
                                                } catch (ClassNotFoundException unused19) {
                                                } catch (NoSuchFieldException unused20) {
                                                } catch (IllegalAccessException unused21) {
                                                }
                                            } catch (ClassNotFoundException unused22) {
                                            } catch (NoSuchFieldException unused23) {
                                            } catch (IllegalAccessException unused24) {
                                            }
                                            f37 = f35;
                                            f36 = f33;
                                        }
                                    } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException unused25) {
                                    }
                                    f37 = f35;
                                    f36 = f33;
                                } else {
                                    f36 = f33;
                                    f37 = f35;
                                }
                                cImage = cImage3;
                                f3 = f34;
                                f2 = f37;
                                f4 = f36;
                                f5 = f32;
                                if (cImage == null) {
                                    synchronized (cImage) {
                                        gLRenderer.pushClip(i26, i27, i24, i25);
                                        if (i2 == 0 && this.Direction == 0) {
                                            if (!this.oncePano) {
                                                double d2 = (double) i25;
                                                i4 = i2;
                                                i5 = i24;
                                                double d3 = (double) i3;
                                                i19 = i27;
                                                i20 = i26;
                                                double sin = (d2 + (Math.sin(0.0d) * d3)) - d3;
                                                i6 = i3;
                                                f31 = f2;
                                                d = 1.0d;
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable1f("fB", ((float) Math.max(1.0d, sin)) / ((float) Math.max(1.0d, (d2 + (Math.sin(1.5707963267948966d) * d3)) - d3)));
                                                gLRenderer.updateVariable1i("pDir", 0);
                                                this.oncePano = true;
                                            } else {
                                                i19 = i27;
                                                i20 = i26;
                                                i6 = i3;
                                                f31 = f2;
                                                i4 = i2;
                                                i5 = i24;
                                                d = 1.0d;
                                            }
                                            gLRenderer.setEffectShader(this.indexShader);
                                            gLRenderer.updateVariable2f("scale", f4, f5);
                                            f9 = f31;
                                            gLRenderer.updateVariable2f("offset", f9, f3);
                                            cImage.setResampling(this.ho.bAntialias);
                                            f8 = f5;
                                            f7 = f4;
                                            f6 = f3;
                                            i8 = i19;
                                            i7 = i20;
                                            gLRenderer.renderImage(cImage, i20, i19, i5, i25, 0, 0);
                                            gLRenderer.removeEffectShader();
                                        } else {
                                            f7 = f4;
                                            f6 = f3;
                                            i8 = i27;
                                            i7 = i26;
                                            i6 = i3;
                                            i4 = i2;
                                            i5 = i24;
                                            f8 = f5;
                                            f9 = f2;
                                            d = 1.0d;
                                        }
                                        if (i4 == 0 && this.Direction == 1) {
                                            if (!this.oncePano) {
                                                i17 = i5;
                                                double d4 = (double) i17;
                                                i18 = i6;
                                                double d5 = (double) i18;
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable1f("fB", ((float) Math.max(d, ((Math.sin(0.0d) * d5) + d4) - d5)) / ((float) Math.max(d, (d4 + (Math.sin(1.5707963267948966d) * d5)) - d5)));
                                                gLRenderer.updateVariable1i("pDir", 1);
                                                this.oncePano = true;
                                            } else {
                                                i18 = i6;
                                                i17 = i5;
                                            }
                                            gLRenderer.setEffectShader(this.indexShader);
                                            f11 = f7;
                                            gLRenderer.updateVariable2f("scale", f11, f8);
                                            gLRenderer.updateVariable2f("offset", f9, f6);
                                            f6 = f6;
                                            i10 = i18;
                                            f10 = f9;
                                            i9 = i17;
                                            gLRenderer.renderImage(cImage, i7, i8, i17, i25, 0, 0);
                                            gLRenderer.removeEffectShader();
                                        } else {
                                            f10 = f9;
                                            i10 = i6;
                                            i9 = i5;
                                            f11 = f7;
                                        }
                                        int i33 = 1;
                                        if (i4 == 1) {
                                            if (this.Direction == 0 && this.PerspectiveDir == 0) {
                                                if (!this.oncePers) {
                                                    double[] LeftBottonSlope = LeftBottonSlope(i10, i9, i25, this.Direction);
                                                    gLRenderer.setEffectShader(this.indexShader);
                                                    gLRenderer.updateVariable1f("fA", (float) LeftBottonSlope[1]);
                                                    gLRenderer.updateVariable1f("fB", (float) LeftBottonSlope[0]);
                                                    gLRenderer.updateVariable1i("pDir", 0);
                                                    this.oncePers = true;
                                                }
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable2f("scale", f11, f8);
                                                gLRenderer.updateVariable2f("offset", f10, f6);
                                                f13 = f6;
                                                f12 = f10;
                                                f14 = f11;
                                                i11 = i4;
                                                gLRenderer.renderImage(cImage, i7, i8, i9, i25, 0, 0);
                                                gLRenderer.removeEffectShader();
                                            } else {
                                                f14 = f11;
                                                f12 = f10;
                                                f13 = f6;
                                                i11 = i4;
                                            }
                                            i33 = 1;
                                        } else {
                                            f14 = f11;
                                            f12 = f10;
                                            f13 = f6;
                                            i11 = i4;
                                        }
                                        if (i11 == i33 && this.Direction == i33) {
                                            if (this.PerspectiveDir == 0) {
                                                if (!this.oncePers) {
                                                    double[] LeftBottonSlope2 = LeftBottonSlope(i10, i9, i25, this.Direction);
                                                    gLRenderer.setEffectShader(this.indexShader);
                                                    if (this.object == null) {
                                                        gLRenderer.updateVariable1f("fA", (float) LeftBottonSlope2[1]);
                                                        gLRenderer.updateVariable1f("fB", (float) LeftBottonSlope2[0]);
                                                        z5 = true;
                                                    } else {
                                                        gLRenderer.updateVariable1f("fA", (float) LeftBottonSlope2[0]);
                                                        z5 = true;
                                                        gLRenderer.updateVariable1f("fB", (float) LeftBottonSlope2[1]);
                                                    }
                                                    int i34 = z5 ? 1 : 0;
                                                    int i35 = z5 ? 1 : 0;
                                                    int i36 = z5 ? 1 : 0;
                                                    gLRenderer.updateVariable1i("pDir", i34);
                                                    this.oncePers = z5;
                                                }
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable2f("scale", f14, f8);
                                                gLRenderer.updateVariable2f("offset", f12, f13);
                                                f16 = f13;
                                                f15 = f12;
                                                f17 = f8;
                                                f18 = f14;
                                                gLRenderer.renderImage(cImage, i7, i8, i9, i25, 0, 0);
                                                gLRenderer.removeEffectShader();
                                            } else {
                                                f17 = f8;
                                                f18 = f14;
                                                f16 = f13;
                                                f15 = f12;
                                            }
                                            i33 = 1;
                                        } else {
                                            f17 = f8;
                                            f18 = f14;
                                            f16 = f13;
                                            f15 = f12;
                                        }
                                        if (i11 == i33 && this.Direction == 0 && this.PerspectiveDir == i33) {
                                            if (!this.oncePers) {
                                                double[] RightTopSlope = RightTopSlope(i10, i9, i25, this.Direction);
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable1f("fA", (float) RightTopSlope[1]);
                                                gLRenderer.updateVariable1f("fB", (float) RightTopSlope[0]);
                                                gLRenderer.updateVariable1i("pDir", 0);
                                                this.oncePers = true;
                                            }
                                            gLRenderer.setEffectShader(this.indexShader);
                                            gLRenderer.updateVariable2f("scale", f18, f17);
                                            gLRenderer.updateVariable2f("offset", f15, f16);
                                            f20 = f16;
                                            f19 = f15;
                                            f21 = f18;
                                            f22 = f17;
                                            gLRenderer.renderImage(cImage, i7, i8, i9, i25, 0, 0);
                                            gLRenderer.removeEffectShader();
                                            i33 = 1;
                                        } else {
                                            f21 = f18;
                                            f22 = f17;
                                            f20 = f16;
                                            f19 = f15;
                                        }
                                        if (i11 == i33 && this.Direction == i33 && this.PerspectiveDir == i33) {
                                            if (!this.oncePers) {
                                                double[] RightTopSlope2 = RightTopSlope(i10, i9, i25, this.Direction);
                                                gLRenderer.setEffectShader(this.indexShader);
                                                if (this.object == null) {
                                                    gLRenderer.updateVariable1f("fA", (float) RightTopSlope2[1]);
                                                    gLRenderer.updateVariable1f("fB", (float) RightTopSlope2[0]);
                                                    z4 = true;
                                                } else {
                                                    gLRenderer.updateVariable1f("fA", (float) RightTopSlope2[0]);
                                                    z4 = true;
                                                    gLRenderer.updateVariable1f("fB", (float) RightTopSlope2[1]);
                                                }
                                                int i37 = z4 ? 1 : 0;
                                                int i38 = z4 ? 1 : 0;
                                                int i39 = z4 ? 1 : 0;
                                                gLRenderer.updateVariable1i("pDir", i37);
                                                this.oncePers = z4;
                                            }
                                            gLRenderer.setEffectShader(this.indexShader);
                                            gLRenderer.updateVariable2f("scale", f21, f22);
                                            gLRenderer.updateVariable2f("offset", f19, f20);
                                            f23 = f20;
                                            i12 = i25;
                                            f24 = f19;
                                            f25 = f21;
                                            gLRenderer.renderImage(cImage, i7, i8, i9, i25, 0, 0);
                                            gLRenderer.removeEffectShader();
                                        } else {
                                            i12 = i25;
                                            f25 = f21;
                                            f23 = f20;
                                            f24 = f19;
                                        }
                                        int i40 = 2;
                                        if (i11 == 2) {
                                            if (this.Direction == 0) {
                                                if (!this.onceSine) {
                                                    gLRenderer.setEffectShader(this.indexShader);
                                                    gLRenderer.updateVariable1f("Zoom", ((float) i10) / f);
                                                    gLRenderer.updateVariable1f("WaveIncrement", ((float) i9) * f43);
                                                    gLRenderer.updateVariable1f("Offset", (float) this.Offset);
                                                    gLRenderer.updateVariable1i("pDir", 0);
                                                    this.onceSine = true;
                                                }
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable2f("scale", f25, f22);
                                                gLRenderer.updateVariable2f("offset", f24, f23);
                                                f27 = f23;
                                                f26 = f24;
                                                gLRenderer.renderImage(cImage, i7, i8, i9, i12, 0, 0);
                                                gLRenderer.removeEffectShader();
                                            } else {
                                                f26 = f24;
                                                f27 = f23;
                                            }
                                            i40 = 2;
                                        } else {
                                            f26 = f24;
                                            f27 = f23;
                                        }
                                        if (i11 == i40 && this.Direction == 1) {
                                            if (!this.onceSine) {
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable1f("Zoom", ((float) i10) / ((float) i9));
                                                gLRenderer.updateVariable1f("WaveIncrement", f43 * f);
                                                gLRenderer.updateVariable1f("Offset", (float) this.Offset);
                                                gLRenderer.updateVariable1i("pDir", 1);
                                                if (this.object == null) {
                                                    gLRenderer.updateVariable1i("Mode", 0);
                                                    z3 = true;
                                                } else {
                                                    z3 = true;
                                                    gLRenderer.updateVariable1i("Mode", 1);
                                                }
                                                this.onceSine = z3;
                                            }
                                            gLRenderer.setEffectShader(this.indexShader);
                                            gLRenderer.updateVariable2f("scale", f25, f22);
                                            gLRenderer.updateVariable2f("offset", f26, f27);
                                            i13 = i9;
                                            f28 = f27;
                                            f29 = f26;
                                            gLRenderer.renderImage(cImage, i7, i8, i9, i12, 0, 0);
                                            gLRenderer.removeEffectShader();
                                        } else {
                                            i13 = i9;
                                            f28 = f27;
                                            f29 = f26;
                                        }
                                        int i41 = 3;
                                        if (i11 == 3) {
                                            if (this.Direction == 0) {
                                                if (!this.onceOffs) {
                                                    gLRenderer.setEffectShader(this.indexShader);
                                                    gLRenderer.updateVariable1f("Zoom", ((float) i10) / f);
                                                    gLRenderer.updateVariable1f("WaveIncrement", f43 * f);
                                                    i16 = i;
                                                    gLRenderer.updateVariable1f("Offset", (float) i16);
                                                    if (this.object == null) {
                                                        gLRenderer.updateVariable1i("Mode", 0);
                                                        z2 = true;
                                                    } else {
                                                        z2 = true;
                                                        gLRenderer.updateVariable1i("Mode", 1);
                                                    }
                                                    gLRenderer.updateVariable1i("pDir", 0);
                                                    this.onceOffs = z2;
                                                } else {
                                                    i16 = i;
                                                }
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable2f("scale", f25, f22);
                                                gLRenderer.updateVariable2f("offset", f29, f28);
                                                f30 = f29;
                                                f28 = f28;
                                                i14 = i16;
                                                gLRenderer.renderImage(cImage, i7, i8, i13, i12, 0, 0);
                                                gLRenderer.removeEffectShader();
                                            } else {
                                                f30 = f29;
                                                i14 = i;
                                            }
                                            i41 = 3;
                                        } else {
                                            f30 = f29;
                                            i14 = i;
                                        }
                                        if (i11 == i41 && this.Direction == 1) {
                                            if (!this.onceOffs) {
                                                gLRenderer.setEffectShader(this.indexShader);
                                                gLRenderer.updateVariable1f("Zoom", ((float) i10) / f);
                                                i15 = i13;
                                                gLRenderer.updateVariable1f("WaveIncrement", ((float) i15) * f43);
                                                gLRenderer.updateVariable1f("Offset", (float) i14);
                                                if (this.object == null) {
                                                    gLRenderer.updateVariable1i("Mode", 0);
                                                    z = true;
                                                } else {
                                                    z = true;
                                                    gLRenderer.updateVariable1i("Mode", 1);
                                                }
                                                int i42 = z ? 1 : 0;
                                                int i43 = z ? 1 : 0;
                                                int i44 = z ? 1 : 0;
                                                gLRenderer.updateVariable1i("pDir", i42);
                                                this.onceOffs = z;
                                            } else {
                                                i15 = i13;
                                            }
                                            gLRenderer.setEffectShader(this.indexShader);
                                            gLRenderer.updateVariable2f("scale", f25, f22);
                                            gLRenderer.updateVariable2f("offset", f30, f28);
                                            gLRenderer.renderImage(cImage, i7, i8, i15, i12, 0, 0);
                                            gLRenderer.removeEffectShader();
                                        }
                                        gLRenderer.popClip();
                                    }
                                    return;
                                }
                                return;
                            }
                        }
                        cImage = cImage3;
                        f4 = f33;
                        f5 = f32;
                        f2 = f35;
                        f3 = f34;
                        if (cImage == null) {
                        }
                    }
                } else {
                    f = f42;
                    i = i30;
                    i3 = i29;
                    i2 = i28;
                    cImage2 = this.imageTexture;
                }
                cImage = cImage2;
                f5 = 1.0f;
                f4 = 1.0f;
                f3 = 0.0f;
                f2 = 0.0f;
                if (cImage == null) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public double[] LeftBottonSlope(int i, int i2, int i3, int i4) {
        int i5;
        double[] dArr = {0.0d, 0.0d};
        int i6 = 0;
        if (i4 == 0) {
            int i7 = 0;
            i5 = 0;
            while (i7 <= i2) {
                double d = (double) i3;
                dArr[i5] = ((d / (((double) (((i7 * i) / i2) + i3)) / d)) + 0.5d) / d;
                i7 += i2;
                i5++;
            }
        } else {
            i5 = 0;
        }
        if (i4 == 1) {
            while (i6 <= i3) {
                double d2 = (double) i2;
                dArr[i5] = ((d2 / (((double) (((i6 * i) / i3) + i2)) / d2)) + 0.5d) / d2;
                i6 += i3;
                i5++;
            }
        }
        return dArr;
    }

    /* access modifiers changed from: package-private */
    public double[] RightTopSlope(int i, int i2, int i3, int i4) {
        int i5;
        double[] dArr = {0.0d, 0.0d};
        int i6 = 0;
        if (i4 == 0) {
            int i7 = 0;
            i5 = 0;
            while (i7 <= i2) {
                double d = (double) i3;
                dArr[i5] = ((d / (((double) ((((i2 - i7) * i) / i2) + i3)) / d)) + 0.5d) / d;
                i7 += i2;
                i5++;
            }
        } else {
            i5 = 0;
        }
        if (i4 == 1) {
            while (i6 <= i3) {
                double d2 = (double) i2;
                dArr[i5] = ((d2 / (((double) ((((i3 - i6) * i) / i3) + i2)) / d2)) + 0.5d) / d2;
                i6 += i3;
                i5++;
            }
        }
        return dArr;
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
            case 0:
                actSetZoomValue(cActExtension);
                return;
            case 1:
                actSetPanorama(cActExtension);
                return;
            case 2:
                actSetPerspective(cActExtension);
                return;
            case 3:
                actSetSineWave(cActExtension);
                return;
            case 4:
                actSetCustom(cActExtension);
                return;
            case 5:
                actSetNumWaves(cActExtension);
                return;
            case 6:
                actSetOffset(cActExtension);
                return;
            case 7:
                actSetHorizontal(cActExtension);
                return;
            case 8:
                actSetVertical(cActExtension);
                return;
            case 9:
                actSetLeftTop(cActExtension);
                return;
            case 10:
                actSetRightBottom(cActExtension);
                return;
            case 11:
                actSetCustomValue(cActExtension);
                return;
            case 12:
                actSetWidth(cActExtension);
                return;
            case 13:
                actSetHeight(cActExtension);
                return;
            case 14:
                actSetResampleOn(cActExtension);
                return;
            case 15:
                actSetResampleOff(cActExtension);
                return;
            case 16:
                actSetSineOffset(cActExtension);
                return;
            case 17:
                actSetCustomOffset(cActExtension);
                return;
            case 18:
                actPausePerspective(cActExtension);
                return;
            case 19:
                actResumePerspective(cActExtension);
                return;
            case 20:
                actUseObject(cActExtension);
                return;
            case 21:
                actAddObject(cActExtension);
                return;
            case 22:
                actAddCoordinates(cActExtension);
                return;
            case 23:
                actRemoveSlot(cActExtension);
                return;
            case 24:
                actResetObject(cActExtension);
                return;
            case 25:
                actDoTransform(cActExtension);
                return;
            case 26:
                actApplyTo(cActExtension);
                return;
            default:
                return;
        }
    }

    @Override // Extensions.CRunExtension
    public CValue expression(int i) {
        switch (i) {
            case 0:
                return expGetZoomValue();
            case 1:
                return expGetOffset();
            case 2:
                return expNumWaves();
            case 3:
                return expGetCustom();
            case 4:
                return expGetWidth();
            case 5:
                return expGetHeight();
            case 6:
                return expGetTPosX();
            case 7:
                return expGetTPosY();
            case 8:
                return expGetTWidth();
            case 9:
                return expGetTHeight();
            case 10:
                return expGetTScaleX();
            case 11:
                return expGetTScaleY();
            default:
                return null;
        }
    }

    private Boolean cndDoneTransformation(CCndExtension cCndExtension) {
        boolean z = false;
        int paramExpression = cCndExtension.getParamExpression(this.rh, 0);
        SparseArray<PosTransform> sparseArray = this.posTransform;
        if (sparseArray == null || sparseArray.get(paramExpression) == null) {
            return false;
        }
        if (this.posTransform.get(paramExpression).SlotNumber == paramExpression) {
            z = true;
        }
        return Boolean.valueOf(this.posTransform.get(paramExpression).done & z);
    }

    private void actSetZoomValue(CActExtension cActExtension) {
        this.ZoomValue = cActExtension.getParamExpression(this.rh, 0);
        if (this.Effect == 0) {
            this.oncePano = false;
        }
        if (this.Effect == 1) {
            this.oncePers = false;
        }
        if (this.Effect == 2) {
            this.onceSine = false;
        }
        if (this.Effect == 3) {
            this.onceOffs = false;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetPanorama(CActExtension cActExtension) {
        if (this.Effect != 0) {
            this.bRemoveShader = true;
        }
        this.Effect = 0;
        this.ho.roc.rcChanged = true;
    }

    private void actSetPerspective(CActExtension cActExtension) {
        if (this.Effect != 1) {
            this.bRemoveShader = true;
        }
        this.Effect = 1;
        this.ho.roc.rcChanged = true;
    }

    private void actSetSineWave(CActExtension cActExtension) {
        if (this.Effect != 2) {
            this.bRemoveShader = true;
        }
        this.Effect = 2;
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
        if (this.Effect == 2) {
            this.onceSine = false;
        }
        if (this.Effect == 3) {
            this.onceOffs = false;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetHorizontal(CActExtension cActExtension) {
        int i = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int i2 = this.ho.hoImgWidth;
        this.Direction = 0;
        this.ho.roc.rcChanged = true;
        int min = Math.min(i, i2);
        int[] iArr = new int[i2];
        for (int i3 = 0; i3 < min; i3++) {
            iArr[i3] = this.CustomArray[i3];
        }
        this.CustomArray = iArr;
    }

    private void actSetVertical(CActExtension cActExtension) {
        int i = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int i2 = this.ho.hoImgHeight;
        this.Direction = 1;
        this.ho.roc.rcChanged = true;
        int min = Math.min(i, i2);
        int[] iArr = new int[i2];
        for (int i3 = 0; i3 < min; i3++) {
            iArr[i3] = this.CustomArray[i3];
        }
        this.CustomArray = iArr;
    }

    private void actSetLeftTop(CActExtension cActExtension) {
        this.PerspectiveDir = 0;
        if (this.Effect == 0) {
            this.oncePano = false;
        }
        if (this.Effect == 1) {
            this.oncePers = false;
        }
        if (this.Effect == 2) {
            this.onceSine = false;
        }
        if (this.Effect == 3) {
            this.onceOffs = false;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetRightBottom(CActExtension cActExtension) {
        this.PerspectiveDir = 1;
        if (this.Effect == 0) {
            this.oncePano = false;
        }
        if (this.Effect == 1) {
            this.oncePers = false;
        }
        if (this.Effect == 2) {
            this.onceSine = false;
        }
        if (this.Effect == 3) {
            this.onceOffs = false;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustomValue(CActExtension cActExtension) {
        int paramExpression = cActExtension.getParamExpression(this.rh, 0);
        int paramExpression2 = cActExtension.getParamExpression(this.rh, 1);
        int i = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        if (paramExpression >= 0 && paramExpression2 < i) {
            this.CustomArray[paramExpression] = paramExpression2;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetWidth(CActExtension cActExtension) {
        this.ho.hoImgWidth = cActExtension.getParamExpression(this.rh, 0);
        resizePerspective(this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight);
    }

    private void actSetHeight(CActExtension cActExtension) {
        this.ho.hoImgHeight = cActExtension.getParamExpression(this.rh, 0);
        resizePerspective(this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight);
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
        if (this.Effect != 3) {
            this.bRemoveShader = true;
        }
        this.Effect = 3;
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
        if (this.Effect == 0) {
            this.oncePano = false;
        }
        if (this.Effect == 1) {
            this.oncePers = false;
        }
        if (this.Effect == 2) {
            this.onceSine = false;
        }
        if (this.Effect == 3) {
            this.onceOffs = false;
        }
        this.object = null;
    }

    private void actUseObject(CActExtension cActExtension) {
        this.object = cActExtension.getParamObject(this.rh, 0);
        if (this.Effect == 0) {
            this.oncePano = false;
        }
        if (this.Effect == 1) {
            this.oncePers = false;
        }
        if (this.Effect == 2) {
            this.onceSine = false;
        }
        if (this.Effect == 3) {
            this.onceOffs = false;
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
                    double d = (double) i9;
                    double d2 = (double) i14;
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
                    double d3 = (double) i3;
                    double d4 = (double) i2;
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
                    double d5 = (double) f3;
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
