package dev.byteplus.render;

import org.lwjgl.opengl.GL33C;

final class GlStateGuard {
   private int program;
   private int vao;
   private int arrayBuffer;
   private int uniformBuffer;
   private int activeTexture;
   private int texture0;
   private int sampler0;
   private int drawFbo;
   private int readFbo;
   private boolean blend;
   private boolean cull;
   private boolean depth;
   private boolean stencil;
   private boolean scissor;
   private int blendSrcRgb;
   private int blendDstRgb;
   private int blendSrcA;
   private int blendDstA;
   private int blendEqRgb;
   private int blendEqA;
   private int cullMode;
   private int frontFace;
   private int depthFunc;
   private boolean depthMask;
   private int stencilFunc;
   private int stencilRef;
   private int stencilValueMask;
   private int stencilWriteMask;
   private int stencilFail;
   private int stencilZFail;
   private int stencilZPass;
   private final int[] viewport = new int[4];
   private final int[] scissorBox = new int[4];
   private final boolean[] colorMask = new boolean[4];
   private int unpackAlign;
   private int unpackRowLength;
   private int unpackSkipPixels;
   private int unpackSkipRows;

   void save() {
      this.program = GL33C.glGetInteger(35725);
      this.vao = GL33C.glGetInteger(34229);
      this.arrayBuffer = GL33C.glGetInteger(34964);
      this.uniformBuffer = GL33C.glGetInteger(35368);
      this.activeTexture = GL33C.glGetInteger(34016);
      GL33C.glActiveTexture(33984);
      this.texture0 = GL33C.glGetInteger(32873);
      this.sampler0 = GL33C.glGetInteger(35097);
      this.drawFbo = GL33C.glGetInteger(36006);
      this.readFbo = GL33C.glGetInteger(36010);
      this.blend = GL33C.glIsEnabled(3042);
      this.blendSrcRgb = GL33C.glGetInteger(32969);
      this.blendDstRgb = GL33C.glGetInteger(32968);
      this.blendSrcA = GL33C.glGetInteger(32971);
      this.blendDstA = GL33C.glGetInteger(32970);
      this.blendEqRgb = GL33C.glGetInteger(32777);
      this.blendEqA = GL33C.glGetInteger(34877);
      this.cull = GL33C.glIsEnabled(2884);
      this.cullMode = GL33C.glGetInteger(2885);
      this.frontFace = GL33C.glGetInteger(2886);
      this.depth = GL33C.glIsEnabled(2929);
      this.depthFunc = GL33C.glGetInteger(2932);
      this.depthMask = GL33C.glGetBoolean(2930);
      this.stencil = GL33C.glIsEnabled(2960);
      this.stencilFunc = GL33C.glGetInteger(2962);
      this.stencilRef = GL33C.glGetInteger(2967);
      this.stencilValueMask = GL33C.glGetInteger(2963);
      this.stencilWriteMask = GL33C.glGetInteger(2968);
      this.stencilFail = GL33C.glGetInteger(2964);
      this.stencilZFail = GL33C.glGetInteger(2965);
      this.stencilZPass = GL33C.glGetInteger(2966);
      this.scissor = GL33C.glIsEnabled(3089);
      GL33C.glGetIntegerv(3088, this.scissorBox);
      GL33C.glGetIntegerv(2978, this.viewport);
      int[] cm = new int[4];
      GL33C.glGetIntegerv(3107, cm);

      for (int i = 0; i < 4; i++) {
         this.colorMask[i] = cm[i] != 0;
      }

      this.unpackAlign = GL33C.glGetInteger(3317);
      this.unpackRowLength = GL33C.glGetInteger(3314);
      this.unpackSkipPixels = GL33C.glGetInteger(3316);
      this.unpackSkipRows = GL33C.glGetInteger(3315);
      GL33C.glActiveTexture(this.activeTexture);
   }

   void restore() {
      GL33C.glUseProgram(this.program);
      GL33C.glBindVertexArray(this.vao);
      GL33C.glBindBuffer(34962, this.arrayBuffer);
      GL33C.glBindBuffer(35345, this.uniformBuffer);
      GL33C.glActiveTexture(33984);
      GL33C.glBindTexture(3553, this.texture0);
      GL33C.glBindSampler(0, this.sampler0);
      GL33C.glActiveTexture(this.activeTexture);
      GL33C.glBindFramebuffer(36009, this.drawFbo);
      GL33C.glBindFramebuffer(36008, this.readFbo);
      set(3042, this.blend);
      GL33C.glBlendFuncSeparate(this.blendSrcRgb, this.blendDstRgb, this.blendSrcA, this.blendDstA);
      GL33C.glBlendEquationSeparate(this.blendEqRgb, this.blendEqA);
      set(2884, this.cull);
      GL33C.glCullFace(this.cullMode);
      GL33C.glFrontFace(this.frontFace);
      set(2929, this.depth);
      GL33C.glDepthFunc(this.depthFunc);
      GL33C.glDepthMask(this.depthMask);
      set(2960, this.stencil);
      GL33C.glStencilFunc(this.stencilFunc, this.stencilRef, this.stencilValueMask);
      GL33C.glStencilMask(this.stencilWriteMask);
      GL33C.glStencilOp(this.stencilFail, this.stencilZFail, this.stencilZPass);
      set(3089, this.scissor);
      GL33C.glScissor(this.scissorBox[0], this.scissorBox[1], this.scissorBox[2], this.scissorBox[3]);
      GL33C.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
      GL33C.glColorMask(this.colorMask[0], this.colorMask[1], this.colorMask[2], this.colorMask[3]);
      GL33C.glPixelStorei(3317, this.unpackAlign);
      GL33C.glPixelStorei(3314, this.unpackRowLength);
      GL33C.glPixelStorei(3316, this.unpackSkipPixels);
      GL33C.glPixelStorei(3315, this.unpackSkipRows);
   }

   private static void set(int cap, boolean on) {
      if (on) {
         GL33C.glEnable(cap);
      } else {
         GL33C.glDisable(cap);
      }
   }
}
