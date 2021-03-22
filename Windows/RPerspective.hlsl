struct PS_INPUT {
  float4 tint : COLOR0;
  float2 texCoord : TEXCOORD0;
};

Texture2D<float4> bgTexture : register(t1);
sampler bg : register(s1);

cbuffer PS_PIXELSIZE : register(b1) {
  float fPixelWidth;
  float fPixelHeight;
};

cbuffer PS_VARIABLES : register(b0) {
  int pDir;
  int orientation;
  int zoom;
  int noWrap;
}

float4 ps_main(in PS_INPUT In) : SV_TARGET {
  float fB;
  
  float2 posTex;
  float4 color = float4(0.0, 0.0, 0.0, 1.0);
  
  if(pDir != 0){
    fB = 1.0 - (zoom * fPixelHeight);
    float y;
    
    if(orientation == 0){
      y = In.texCoord.y;
    } else {
      y = 1.0 - In.texCoord.y;
    }
    
    float ScreenX = (1.0 - (1.0 - fB) * y);
    posTex = In.texCoord * float2(ScreenX, 1.0) + float2((1.0 - ScreenX) / 2.0, 0.0);
    
    if(noWrap == 0 || (posTex.x >= 0.0 && posTex.x <= 1.0)){
      color = bgTexture.Sample(bg, posTex);
    }
  } else {
    fB = 1.0 - (zoom * fPixelWidth);
    float x;
    
    if(orientation == 0){
      x = In.texCoord.x;
    } else {
      x = 1.0 - In.texCoord.x;
    }
    
    float ScreenY = (1.0 - (1.0 - fB) * x);
    posTex = In.texCoord * float2(1.0, ScreenY) + float2(0.0, (1.0 - ScreenY) / 2.0);
    
    if(noWrap == 0 || (posTex.y >= 0.0 && posTex.y <= 1.0)){
      color = bgTexture.Sample(bg, posTex);
    }
  }
  
  return color;
}