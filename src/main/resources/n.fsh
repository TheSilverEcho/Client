#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 resolution;

#define iTime time
#define iResolution resolution


void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    float r, g, b;

    r=0.2;
    g=0.2;
    b=5.57;

    float time=iTime*8.0;
    float ot1=5.0;
    float ot3=2.0;
    float ot5=0.1;
    float ot7=0.01;
    float ot9=0.025;
    float Q=5000.;
    vec2 uv = fragCoord/iResolution.xy;
    float amnt;
    float nd;
    float ip;
    float alpha;
    vec4 cbuff = vec4(0.0);
    for (float i=0.0; i<10.0;i++){

        ip=i-2.0;
        nd = 2.0/4.0*ot1*sin(uv.x*2.0*3.14+ip*0.4+time*0.05)/2.0;

        nd += 1.0/4.0*ot3*sin(3.0*uv.x*2.0*3.14+ip*0.4)/2.0;

        nd += 1.0/4.0*ot5*sin(5.0*uv.x*2.0*3.14+ip*0.4)/2.0;

        nd += 1.0/4.0*ot7*sin(7.0*uv.x*2.0*3.14+ip*0.4)/2.0;

        nd/=5.0;
        nd+=0.5;
        amnt = 1.0/abs(nd-uv.y)*0.01;
        amnt= smoothstep(0.01, 0.5+10.0*uv.y, amnt)*5.5;
        alpha=(10.0-i)/5.0;
        cbuff += vec4(amnt*alpha*0.3, amnt*0.3*alpha, amnt*uv.y*alpha, 0);
    }


    fragColor = vec4(cbuff[0]*r, cbuff[1]*g, cbuff[2]*b, 1.0);

}

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}