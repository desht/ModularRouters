package me.desht.modularrouters.client.render.item_beam;

public enum ItemBeamDispatcher {
    INSTANCE;

//    private static final int MAX_SIZE = 250;
//
//    public final List<ItemBeam> beams = new ArrayList<>();
//
//    public static ItemBeamDispatcher getInstance() {
//        return INSTANCE;
//    }
//
//    public void addBeam(ItemBeam itemBeam) {
//        if (beams.size() < MAX_SIZE) {
//            beams.add(itemBeam);
//        }
//    }
//
//    public void tick() {
//        for (Iterator<ItemBeam> iterator = beams.iterator(); iterator.hasNext(); ) {
//            ItemBeam beam = iterator.next();
//            beam.tick();
//            if (beam.isExpired()) iterator.remove();
//        }
//    }
//
//    @SubscribeEvent
//    public void onWorldRender(RenderWorldLastEvent event) {
//        if (beams.isEmpty()) return;
//
//        IProfiler profiler = Minecraft.getInstance().getProfiler();
//        profiler.startSection("modularrouters-particles");
//
//        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
//        MatrixStack matrixStack = event.getMatrixStack();
//
//        matrixStack.push();
//
//        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
//        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
//
//        for (ItemBeam beam: beams) {
//            beam.render(matrixStack, buffer, event.getPartialTicks());
//        }
//
//        matrixStack.pop();
//
//        profiler.endSection();
//    }
//
//    @SubscribeEvent
//    public void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase == TickEvent.Phase.START) {
//            INSTANCE.tick();
//        }
//    }
}
