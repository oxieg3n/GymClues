param([Parameter(Mandatory=$true)][string]$FFmpeg, [string]$Timeline)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System;
using System.IO;
using System.Text;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Diagnostics;
using System.Runtime.InteropServices;

public static class GymCluesTour {
  const int W = 1080, H = 2400, Fps = 30;
  static readonly Color Green = Color.FromArgb(4,247,48);
  public class Scene {
    public double Start, End; public string Title, Voice; public RectangleF Focus;
    public string Image, ScrollTo; public int ScrollOffset;
    public double ClickAt=-1; public PointF Pointer;
    public Scene(double a,double b,string title,string voice,int x,int y,int w,int h) {
      Start=a; End=b; Title=title; Voice=voice; Focus=new RectangleF(x,y,w,h);
    }
  }
  static Scene[] Scenes = {
    new Scene(2,5,"Your gym, your starting point","Welcome to GymClues. Choose your gym here.",42,83,780,128),
    new Scene(5,8,"Music within reach","Connect music and keep playback within reach.",50,232,980,137),
    new Scene(8,13,"Today's training","Today's training shows your exercises and working sets. Start here, or preview first.",50,624,980,749),
    new Scene(13,16,"Build your training rhythm","Training rhythm tracks workout days and your streak.",50,1407,980,599),
    new Scene(16,19,"Follow your program progress","Program progress counts the sessions you've completed.",50,2046,980,290),
    new Scene(19,23,"Plan the days ahead","Scroll for upcoming workouts, calendar views, and rest days.",50,1560,980,735),
    new Scene(23,25,"Open Training & tools","Open the menu for more.",900,83,128,128),
    new Scene(25,29,"Start a workout","Start a workout from a program, or build your own.",285,511,720,123),
    new Scene(29,31,"Your completed sessions","Review your completed sessions.",285,640,720,115),
    new Scene(31,33,"Your active program","Return to your active program.",285,766,720,118),
    new Scene(33,36,"Exercise & equipment guides","Explore exercise guides and equipment setup instructions.",285,894,720,115),
    new Scene(36,39,"Make it your own","Add personal exercises in My Exercises.",285,1149,720,115),
    new Scene(39,42,"Five themes to choose from","Choose your favorite of five color themes.",285,1275,720,115),
    new Scene(42,45,"You're ready to train","Admin manages the catalog. You're ready to train.",285,1435,720,115)
  };
  static double Ease(double x) { x=Math.Max(0,Math.Min(1,x)); return x*x*(3-2*x); }
  static GraphicsPath Rounded(RectangleF r,float radius) {
    GraphicsPath p=new GraphicsPath(); float d=radius*2;
    p.AddArc(r.X,r.Y,d,d,180,90); p.AddArc(r.Right-d,r.Y,d,d,270,90);
    p.AddArc(r.Right-d,r.Bottom-d,d,d,0,90); p.AddArc(r.X,r.Bottom-d,d,d,90,90); p.CloseFigure(); return p;
  }
  static void ImageAt(Graphics g,Image im,float y) { g.DrawImage(im,new RectangleF(0,y,W,H),new RectangleF(0,0,W,H),GraphicsUnit.Pixel); }
  static void Base(Graphics g,Image home,Image schedule,Image menu,double t) {
    if(t>=24.2) { ImageAt(g,menu,0); }
    else {
      ImageAt(g,home,0);
      double offset=816*Ease((t-19)/0.7)*(1-Ease((t-23)/0.5));
      if(offset>0) {
        GraphicsState save=g.Save(); g.SetClip(new Rectangle(0,380,W,1957));
        g.DrawImage(home,new RectangleF(0,380-(float)offset,W,1957),new RectangleF(0,380,W,1957),GraphicsUnit.Pixel);
        g.DrawImage(schedule,new RectangleF(0,2337-(float)offset,W,816),new RectangleF(0,1521,W,816),GraphicsUnit.Pixel);
        g.Restore(save);
      }
    }
    g.DrawImage(home,new Rectangle(0,0,W,380),new Rectangle(0,0,W,380),GraphicsUnit.Pixel);
    g.DrawImage(home,new Rectangle(0,2337,W,63),new Rectangle(0,2337,W,63),GraphicsUnit.Pixel);
  }
  static RectangleF Focus(Scene s,double t) {
    if(t>=33 && t<36) return t<34.5 ? new RectangleF(285,894,720,115) : new RectangleF(285,1021,720,115);
    if(t>=43.4) return new RectangleF(264,389,784,1189);
    if(t>=24.2 && t<25) return new RectangleF(264,389,784,1189);
    return s.Focus;
  }
  static PointF Target(RectangleF r,double t) {
    if(t>=23 && t<25) return new PointF(964,148);
    if(t>=8 && t<13) return new PointF(790,1080);
    if(t>=19 && t<23) return new PointF(950,1615);
    return new PointF(r.Right-55,r.Y+r.Height*0.55f);
  }
  static void Hand(Graphics g,PointF tip,double t,double clickAt=24) {
    GraphicsState save=g.Save(); g.TranslateTransform(tip.X,tip.Y);
    float press=(float)(1-0.12*Math.Exp(-Math.Pow((t-clickAt)/0.09,2)));
    g.ScaleTransform(press,press); g.RotateTransform(-12);
    using(GraphicsPath p=new GraphicsPath()) {
      p.StartFigure(); p.AddBezier(0,0,-11,-1,-17,8,-17,20); p.AddLine(-17,20,-17,66);
      p.AddBezier(-17,66,-34,47,-47,52,-43,69); p.AddLine(-43,69,-15,115);
      p.AddBezier(-15,115,-8,125,7,131,23,131); p.AddLine(23,131,51,129);
      p.AddBezier(51,129,69,125,80,112,80,94); p.AddLine(80,94,80,56);
      p.AddBezier(80,56,79,40,66,39,60,47); p.AddBezier(60,47,59,29,44,27,37,40);
      p.AddBezier(37,40,33,23,18,24,14,37); p.AddLine(14,37,14,18);
      p.AddBezier(14,18,14,7,10,0,0,0); p.CloseFigure();
      using(Pen halo=new Pen(Color.FromArgb(150,4,247,48),13)) g.DrawPath(halo,p);
      using(Brush white=new SolidBrush(Color.White)) g.FillPath(white,p);
      using(Pen edge=new Pen(Color.FromArgb(24,30,26),5)) g.DrawPath(edge,p);
      using(Pen crease=new Pen(Color.FromArgb(90,105,95),3)) {
        g.DrawLine(crease,14,43,14,74); g.DrawLine(crease,37,46,37,77); g.DrawLine(crease,60,53,60,82);
      }
    }
    g.Restore(save);
    double click=(t-clickAt)/0.6;
    if(click>=0 && click<=1) using(Pen ripple=new Pen(Color.FromArgb((int)(255*(1-click)),4,247,48),6)) {
      float radius=(float)(18+click*65); g.DrawEllipse(ripple,tip.X-radius,tip.Y-radius,radius*2,radius*2);
    }
  }
  static string Stamp(double t) { return TimeSpan.FromSeconds(t).ToString(@"hh\:mm\:ss\,fff"); }
  public static void RenderSteps(string root,string ffmpeg,string name,string label,Scene[] steps) {
    string demo=Path.Combine(root,"docs/demo"), build=Path.Combine(root,"build/demo-tour");
    System.Collections.Generic.Dictionary<string,Image> images=new System.Collections.Generic.Dictionary<string,Image>();
    double end=2; int words=0, cue=0; string lastVoice=null;
    StringBuilder plain=new StringBuilder(), timed=new StringBuilder("# "+label+"\n\n45 seconds total, including a silent two-second logo opening. Generate each narration segment separately and align it to the cue below.\n\n| Time | Feature | Voiceover |\n| --- | --- | --- |\n"), srt=new StringBuilder();
    try {
      for(int i=0;i<steps.Length;i++) {
        Scene s=steps[i];
        if(s.Start!=end || s.End<=s.Start || s.Focus.X<0 || s.Focus.Y<0 || s.Focus.Right>W || s.Focus.Bottom>H || !s.Focus.Contains(s.Pointer)) throw new Exception("Invalid scene: "+s.Title);
        if(s.ClickAt>=0 && (s.ClickAt<s.Start || s.ClickAt>=s.End)) throw new Exception("Click outside scene: "+s.Title);
        if(s.ScrollTo!=null && (s.ScrollOffset<=0 || s.ScrollOffset>1957)) throw new Exception("Invalid scroll distance: "+s.Title);
        end=s.End;
        foreach(string file in new string[]{s.Image,s.ScrollTo}) if(file!=null && !images.ContainsKey(file)) {
          Image image=Image.FromFile(Path.Combine(demo,"source",file)); images.Add(file,image);
          if(image.Width!=W || image.Height!=H) throw new Exception("Capture size changed: "+file);
        }
        if(s.Voice==lastVoice) continue;
        double voiceEnd=s.End; int j=i+1; while(j<steps.Length && steps[j].Voice==s.Voice) voiceEnd=steps[j++].End;
        int count=s.Voice.Split(' ').Length; words+=count;
        if(count*60/(voiceEnd-s.Start)>195) throw new Exception("Narration is too fast: "+s.Title);
        plain.Append(s.Voice+"\n\n"); timed.Append("| "+s.Start.ToString("00")+"-"+voiceEnd.ToString("00")+"s | "+s.Title+" | "+s.Voice+" |\n");
        srt.Append((++cue)+"\n"+Stamp(s.Start)+" --> "+Stamp(voiceEnd)+"\n"+s.Voice+"\n\n"); lastVoice=s.Voice;
      }
      if(end!=45 || words>120) throw new Exception("Expected 45 seconds and at most 120 narration words.");
      File.WriteAllText(Path.Combine(demo,name+"-voiceover.txt"),plain.ToString(),new UTF8Encoding(false));
      File.WriteAllText(Path.Combine(demo,name+"-timed.md"),timed.ToString(),new UTF8Encoding(false));
      File.WriteAllText(Path.Combine(demo,name+".srt"),srt.ToString(),new UTF8Encoding(false));
      Directory.CreateDirectory(build);
      ProcessStartInfo info=new ProcessStartInfo(ffmpeg,"-y -hide_banner -loglevel error -f rawvideo -pixel_format bgr24 -video_size 1080x2400 -framerate 30 -i pipe:0 -an -c:v libx264 -preset veryfast -crf 19 -pix_fmt yuv420p -movflags +faststart \""+Path.Combine(build,name+".mp4")+"\"");
      info.UseShellExecute=false; info.RedirectStandardInput=true; info.CreateNoWindow=true;
      using(Bitmap frame=new Bitmap(W,H,PixelFormat.Format24bppRgb))
      using(Image logo=Image.FromFile(Path.Combine(root,"app/src/main/res/drawable/gymclues_logo.png")))
      using(Font title=new Font("Segoe UI",38,FontStyle.Bold,GraphicsUnit.Pixel))
      using(Font body=new Font("Segoe UI",35,FontStyle.Regular,GraphicsUnit.Pixel))
      using(Font small=new Font("Segoe UI",24,FontStyle.Bold,GraphicsUnit.Pixel))
      using(Process process=Process.Start(info)) {
        byte[] pixels=new byte[W*H*3]; PointF cursor=steps[0].Pointer; int index=0;
        try {
          for(int n=0;n<45*Fps;n++) {
            double t=n/(double)Fps;
            using(Graphics g=Graphics.FromImage(frame)) {
              g.SmoothingMode=SmoothingMode.AntiAlias; g.InterpolationMode=InterpolationMode.HighQualityBicubic; g.TextRenderingHint=System.Drawing.Text.TextRenderingHint.AntiAliasGridFit; g.Clear(Color.Black);
              if(t<2) {
                g.DrawImage(logo,300,840,480,480);
                using(Brush green=new SolidBrush(Green)) g.DrawString("GymClues",title,green,new RectangleF(100,1370,880,90),new StringFormat{Alignment=StringAlignment.Center});
              } else {
                while(t>=steps[index].End) index++;
                Scene s=steps[index];
                if(s.ScrollTo!=null) {
                  double offset=s.ScrollOffset*Ease((t-s.Start)/0.7);
                  // shortcut: scrolling joins two fixed-size captures; recapture if the UI layout changes.
                  ImageAt(g,images[s.Image],0);
                  GraphicsState saved=g.Save(); g.SetClip(new Rectangle(0,380,W,1957));
                  g.DrawImage(images[s.Image],new RectangleF(0,380-(float)offset,W,1957),new RectangleF(0,380,W,1957),GraphicsUnit.Pixel);
                  g.DrawImage(images[s.ScrollTo],new RectangleF(0,2337-(float)offset,W,s.ScrollOffset),new RectangleF(0,2337-s.ScrollOffset,W,s.ScrollOffset),GraphicsUnit.Pixel);
                  g.Restore(saved);
                  g.DrawImage(images[s.Image],new Rectangle(0,0,W,380),new Rectangle(0,0,W,380),GraphicsUnit.Pixel);
                } else ImageAt(g,images[s.Image],0);
                Image status=images[steps[0].Image];
                g.DrawImage(status,new Rectangle(0,0,W,80),new Rectangle(0,0,W,80),GraphicsUnit.Pixel);
                using(GraphicsPath hole=Rounded(s.Focus,22)) using(Region shade=new Region(new Rectangle(0,0,W,H))) {
                  shade.Exclude(hole); using(Brush dim=new SolidBrush(Color.FromArgb(205,0,0,0))) g.FillRegion(dim,shade);
                  using(Pen glow=new Pen(Color.FromArgb(60,4,247,48),14)) g.DrawPath(glow,hole);
                  using(Pen ring=new Pen(Green,4)) g.DrawPath(ring,hole);
                }
                cursor.X+=(s.Pointer.X-cursor.X)*0.14f; cursor.Y+=(s.Pointer.Y-cursor.Y)*0.14f;
                PointF tip=cursor; tip.Y+=(float)(Math.Sin(t*3.2)*4); Hand(g,tip,t,s.ClickAt);
                float cardY=s.Focus.Bottom>1880 && s.Focus.Y>830 ? 430 : 1920;
                using(GraphicsPath box=Rounded(new RectangleF(70,cardY,940,370),24)) {
                  using(Brush bg=new SolidBrush(Color.FromArgb(248,12,18,14))) g.FillPath(bg,box);
                  using(Pen outline=new Pen(Color.FromArgb(65,130,77),2)) g.DrawPath(outline,box);
                }
                using(Brush green=new SolidBrush(Green)) {
                  g.DrawString(label.ToUpperInvariant(),small,green,100,cardY+27);
                  g.DrawString(s.Title,title,Brushes.White,new RectangleF(100,cardY+77,880,95));
                  g.DrawString(s.Voice,body,Brushes.White,new RectangleF(100,cardY+180,880,148));
                  g.FillRectangle(green,100,cardY+347,(float)(880*t/45),4);
                }
                if(n==(int)((s.Start+Math.Min(0.5,(s.End-s.Start)/2))*Fps)) frame.Save(Path.Combine(build,name+"-scene-"+index+".png"),ImageFormat.Png);
                if(n==330) frame.Save(Path.Combine(demo,"screens",name+"-poster.png"),ImageFormat.Png);
              }
            }
            BitmapData data=frame.LockBits(new Rectangle(0,0,W,H),ImageLockMode.ReadOnly,PixelFormat.Format24bppRgb);
            try { Marshal.Copy(data.Scan0,pixels,0,pixels.Length); } finally { frame.UnlockBits(data); }
            process.StandardInput.BaseStream.Write(pixels,0,pixels.Length);
            if(n%300==0) Console.WriteLine(name+": "+(n/Fps)+" / 45 seconds");
          }
        } finally { process.StandardInput.Close(); process.WaitForExit(); }
        if(process.ExitCode!=0) throw new Exception("Video encoder failed: "+process.ExitCode);
      }
      Console.WriteLine(name+": validated 1350 frames, 45 seconds, "+words+" narration words.");
    } finally { foreach(Image image in images.Values) image.Dispose(); }
  }
  public static void Render(string root,string ffmpeg) {
    double last=2; int words=0;
    foreach(Scene s in Scenes) {
      if(s.Start!=last || s.End<=s.Start || s.Focus.X<0 || s.Focus.Right>W || s.Focus.Y<0 || s.Focus.Bottom>H) throw new Exception("Invalid timeline: "+s.Title);
      words+=s.Voice.Split(' ').Length; last=s.End;
    }
    if(last!=45 || words>120) throw new Exception("Timeline must be 45 seconds and readable at narration speed.");
    string demo=Path.Combine(root,"docs/demo"), build=Path.Combine(root,"build/demo-tour");
    Directory.CreateDirectory(build);
    StringBuilder script=new StringBuilder(), timed=new StringBuilder("# GymClues home and menu walkthrough\n\n45 seconds total. Hold the logo silently from 00:00 to 00:02. Read the remaining "+words+" words at approximately 145 words per minute, aligning each sentence to its cue. Generate the segments separately if your TTS service cannot control pauses.\n\n| Time | Feature | Voiceover |\n| --- | --- | --- |\n"), srt=new StringBuilder();
    int cue=1;
    foreach(Scene s in Scenes) {
      script.Append(s.Voice+"\n\n");
      timed.Append("| "+s.Start.ToString("00")+"-"+s.End.ToString("00")+"s | "+s.Title+" | "+s.Voice+" |\n");
      srt.Append(cue+++"\n"+Stamp(s.Start)+" --> "+Stamp(s.End)+"\n"+s.Voice+"\n\n");
    }
    File.WriteAllText(Path.Combine(demo,"voiceover.txt"),script.ToString(),new UTF8Encoding(false));
    File.WriteAllText(Path.Combine(demo,"voiceover-timed.md"),timed.ToString(),new UTF8Encoding(false));
    File.WriteAllText(Path.Combine(demo,"home-menu-tour.srt"),srt.ToString(),new UTF8Encoding(false));
    string output=Path.Combine(build,"home-menu-tour.mp4");
    ProcessStartInfo info=new ProcessStartInfo(ffmpeg,"-y -hide_banner -loglevel error -f rawvideo -pixel_format bgr24 -video_size 1080x2400 -framerate 30 -i pipe:0 -an -c:v libx264 -preset veryfast -crf 19 -pix_fmt yuv420p -movflags +faststart \""+output+"\"");
    info.UseShellExecute=false; info.RedirectStandardInput=true; info.CreateNoWindow=true;
    using(Image home=Image.FromFile(Path.Combine(demo,"source/home.png")))
    using(Image schedule=Image.FromFile(Path.Combine(demo,"source/home-schedule.png")))
    using(Image menu=Image.FromFile(Path.Combine(demo,"source/menu.png")))
    using(Image logo=Image.FromFile(Path.Combine(root,"app/src/main/res/drawable/gymclues_logo.png")))
    using(Bitmap frame=new Bitmap(W,H,PixelFormat.Format24bppRgb))
    using(Font title=new Font("Segoe UI",38,FontStyle.Bold,GraphicsUnit.Pixel))
    using(Font body=new Font("Segoe UI",35,FontStyle.Regular,GraphicsUnit.Pixel))
    using(Font small=new Font("Segoe UI",24,FontStyle.Bold,GraphicsUnit.Pixel))
    using(Process process=Process.Start(info)) {
      foreach(Image im in new Image[]{home,schedule,menu}) if(im.Width!=W || im.Height!=H) throw new Exception("Capture size changed; update highlight coordinates.");
      byte[] pixels=new byte[W*H*3]; PointF cursor=new PointF(800,250);
      for(int n=0;n<45*Fps;n++) {
        double t=n/(double)Fps;
        using(Graphics g=Graphics.FromImage(frame)) {
          g.SmoothingMode=SmoothingMode.AntiAlias; g.InterpolationMode=InterpolationMode.HighQualityBicubic; g.TextRenderingHint=System.Drawing.Text.TextRenderingHint.AntiAliasGridFit;
          g.Clear(Color.Black);
          if(t<2) {
            g.DrawImage(logo,300,840,480,480);
            using(Brush green=new SolidBrush(Green)) g.DrawString("GymClues",title,green,new RectangleF(100,1370,880,90),new StringFormat{Alignment=StringAlignment.Center});
            if(n==30) frame.Save(Path.Combine(demo,"screens/tour-launch.png"),ImageFormat.Png);
          } else {
            Base(g,home,schedule,menu,t);
            int index=0; while(t>=Scenes[index].End) index++;
            Scene scene=Scenes[index]; RectangleF focus=Focus(scene,t);
            using(GraphicsPath hole=Rounded(focus,22)) using(Region shade=new Region(new Rectangle(0,0,W,H))) {
              shade.Exclude(hole); using(Brush dim=new SolidBrush(Color.FromArgb(205,0,0,0))) g.FillRegion(dim,shade);
              using(Pen glow=new Pen(Color.FromArgb(60,4,247,48),14)) g.DrawPath(glow,hole);
              using(Pen ring=new Pen(Green,4)) g.DrawPath(ring,hole);
            }
            PointF target=Target(focus,t);
            cursor.X+=(target.X-cursor.X)*0.14f; cursor.Y+=(target.Y-cursor.Y)*0.14f;
            PointF tip=cursor;
            tip.Y+=(float)(Math.Sin(t*3.2)*4);
            Hand(g,tip,t);
            float cardY=focus.Bottom>1950 && t<23 ? 430 : 1920;
            RectangleF card=new RectangleF(70,cardY,940,370);
            using(GraphicsPath box=Rounded(card,24)) {
              using(Brush bg=new SolidBrush(Color.FromArgb(248,12,18,14))) g.FillPath(bg,box);
              using(Pen outline=new Pen(Color.FromArgb(65,130,77),2)) g.DrawPath(outline,box);
            }
            using(Brush green=new SolidBrush(Green)) {
              g.DrawString(t<25?"HOME TOUR":"TRAINING & TOOLS",small,green,100,cardY+27);
              g.DrawString(scene.Title,title,Brushes.White,new RectangleF(100,cardY+77,880,95));
              g.DrawString(scene.Voice,body,Brushes.White,new RectangleF(100,cardY+180,880,148));
              g.FillRectangle(green,100,cardY+347,(float)(880*t/45),4);
            }
            if(n==270) frame.Save(Path.Combine(demo,"screens/tour-poster.png"),ImageFormat.Png);
          }
          if(n==120 || n==510 || n==630 || n==725 || n==810 || n==1050 || n==1290) frame.Save(Path.Combine(build,"review-"+n+".png"),ImageFormat.Png);
        }
        BitmapData data=frame.LockBits(new Rectangle(0,0,W,H),ImageLockMode.ReadOnly,PixelFormat.Format24bppRgb);
        try { Marshal.Copy(data.Scan0,pixels,0,pixels.Length); } finally { frame.UnlockBits(data); }
        process.StandardInput.BaseStream.Write(pixels,0,pixels.Length);
        if(n%300==0) Console.WriteLine("Rendered "+(n/Fps)+" / 45 seconds");
      }
      process.StandardInput.Close(); process.WaitForExit();
      if(process.ExitCode!=0) throw new Exception("Video encoder failed: "+process.ExitCode);
    }
    Console.WriteLine("Rendered 1350 frames; 45 seconds. Narration: "+words+" words. Output: "+output);
  }
}
'@
$repoDemo = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
if ($Timeline) {
  $tourDemo = Get-Content -Raw -Encoding UTF8 $Timeline | ConvertFrom-Json
  $stepsDemo = foreach ($stepDemo in $tourDemo.steps) {
    $sceneDemo = [GymCluesTour+Scene]::new($stepDemo.start,$stepDemo.end,$stepDemo.title,$stepDemo.voice,$stepDemo.focus[0],$stepDemo.focus[1],$stepDemo.focus[2],$stepDemo.focus[3])
    $sceneDemo.Image = $stepDemo.image
    $sceneDemo.Pointer = [System.Drawing.PointF]::new($stepDemo.pointer[0],$stepDemo.pointer[1])
    if ($stepDemo.click) { $sceneDemo.ClickAt = $stepDemo.end - 0.45 }
    if ($stepDemo.scrollTo) { $sceneDemo.ScrollTo = $stepDemo.scrollTo; $sceneDemo.ScrollOffset = $stepDemo.offset }
    $sceneDemo
  }
  [GymCluesTour]::RenderSteps($repoDemo,(Resolve-Path $FFmpeg).Path,$tourDemo.name,$tourDemo.label,[GymCluesTour+Scene[]]$stepsDemo)
} else { [GymCluesTour]::Render($repoDemo, (Resolve-Path $FFmpeg).Path) }
