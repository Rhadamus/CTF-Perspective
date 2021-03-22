precision mediump float;

uniform mediump sampler2D texture;
uniform lowp float inkParam;
uniform lowp int inkEffect;
uniform lowp vec3 rgbCoeff;

uniform float fB;
uniform lowp int pDir;

varying mediump vec2 texCoordinate;

void main()
{
	vec2 posTex;
	float fC;
	
	lowp vec4 color = vec4(0.0, 0.0, 0.0, 1.0);
	
	if(pDir == 0)
	{
		fC =  max(0.02, 1.0+(fB - 1.0)*4.0*pow((texCoordinate.s-0.5),2.0));
		posTex = texCoordinate * vec2(1.0, fC) + vec2(0.0, (1.0-fC)/2.0);
		color = texture2D(texture, posTex) * vec4(rgbCoeff, inkParam);
	}
	
	if(pDir == 1)
	{
		fC =  max(0.05, 1.0+(fB - 1.0)*4.0*pow((texCoordinate.t-0.5),2.0));
		posTex = texCoordinate * vec2(fC, 1.0) + vec2((1.0-fC)/2.0, 0.0);
		color = texture2D(texture, posTex) * vec4(rgbCoeff, inkParam);
	}
		
	//color = texture2D(texture, posTex) * vec4(rgbCoeff, inkParam);

	if(inkEffect == 2)			//INVERT
		color.rgb = vec3(1,1,1)-color.rgb;
	else if(inkEffect == 10)	//MONO
	{
		lowp float mono = 0.3125*color.r + 0.5625*color.g + 0.125*color.b;
		color.rgb = vec3(mono,mono,mono);
	}
	
	gl_FragColor = color;
}
