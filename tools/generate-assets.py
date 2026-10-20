#!/usr/bin/env python3
"""Regenerate checked-in bitmap atlas with Pillow + Noto Sans CJK; runtime needs neither."""
import pathlib, math, wave, struct
from PIL import Image, ImageDraw, ImageFont
root=pathlib.Path(__file__).resolve().parents[1]
font=ImageFont.truetype('/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc',64,index=0)
text=''.join(p.read_text() for p in (root/'core/src/main/java').rglob('*.java'))
chars=sorted(set(chr(i) for i in range(32,127))|set(c for c in text if ord(c)>127))
cell=80;cols=24;rows=math.ceil(len(chars)/cols)
height=2**math.ceil(math.log2(rows*cell));width=2048
img=Image.new('RGBA',(width,height));draw=ImageDraw.Draw(img)
lines=['info face="Noto Sans CJK JP" size=64 bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1',f'common lineHeight=96 base=76 scaleW={width} scaleH={height} pages=1 packed=0','page id=0 file="ui.png"',f'chars count={len(chars)}']
for i,c in enumerate(chars):
 x=(i%cols)*cell+3;y=(i//cols)*cell+3
 left,top,right,bottom=font.getbbox(c);w=right-left;h=bottom-top
 draw.text((x-left,y-top),c,font=font,fill='white')
 lines.append(f'char id={ord(c)} x={x} y={y} width={w} height={h} xoffset={left} yoffset={top} xadvance={round(font.getlength(c))} page=0 chnl=15')
(root/'assets/ui.fnt').write_text('\n'.join(lines)+'\n');img.save(root/'assets/ui.png')
for name,freq,duration in [('hit',660,.035),('complete',990,.16)]:
 with wave.open(str(root/'assets'/f'{name}.wav'),'wb') as wav:
  wav.setnchannels(1);wav.setsampwidth(2);wav.setframerate(22050)
  wav.writeframes(b''.join(struct.pack('<h',int(5000*math.sin(2*math.pi*freq*i/22050)*(1-i/(22050*duration))**2)) for i in range(int(22050*duration))))
print(f'{len(chars)} glyphs; atlas {width}x{height}; 2 synthesized sounds')
