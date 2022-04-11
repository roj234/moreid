package moreid.asm;

import ilib.api.ContextClassTransformer;
import moreid.Config;
import roj.asm.Opcodes;
import roj.asm.cst.Constant;
import roj.asm.cst.CstInt;
import roj.asm.tree.ConstantData;
import roj.asm.tree.Method;
import roj.asm.tree.insn.ClassInsnNode;
import roj.asm.tree.insn.InsnNode;
import roj.asm.tree.insn.InvokeInsnNode;
import roj.asm.tree.insn.NPInsnNode;
import roj.asm.util.Context;
import roj.asm.util.InsnList;

import java.util.List;

/**
 * @author Roj233
 * @since 2022/4/9 19:16
 */
class Transformer implements ContextClassTransformer {
    @Override
    public void transform(String trsName, Context ctx) {
        switch (trsName) {
            case "net.minecraft.stat.StatList":
                statListMax(ctx);
                break;
            case "net.minecraft.world.chunk.Chunk":
                if (Config.tinyTileMap) doTinyTileMap(ctx);
                break;
        }
    }

    private static void statListMax(Context ctx) {
        ConstantData data = ctx.getData();
        int found = 0;
        List<Constant> array = data.cp.array();
        for (int i = 0; i < array.size(); i++) {
            Constant c = array.get(i);
            if (c.type() == Constant.INT) {
                CstInt cst = (CstInt) c;
                switch (cst.value) {
                    case 32000:
                        cst.value = Config.itemMax;
                        found |= 1;
                        break;
                    case 4096:
                        cst.value = Config.blockMax;
                        found |= 2;
                        break;
                }
                if (found == 3) {
                    return;
                }
            }
        }
        throw new RuntimeException("Failed to find [LDC int] tag in StatList");
    }

    private static void doTinyTileMap(Context ctx) {
        ConstantData data = ctx.getData();
        Method init = data.getUpgradedMethod("<init>", "(Lnet/minecraft/world/World;II)V");
        if (init != null) {
            InsnList insn = init.code.instructions;
            for (int j = 0; j < insn.size(); j++) {
                InsnNode node = insn.get(j);
                if (node.getOpcode() == Opcodes.INVOKESTATIC) {
                    InvokeInsnNode node1 = (InvokeInsnNode) node;
                    if (node1.name.equals("newHashMap")) {
                        String pmap = "moreid/util/FastTileMap";
                        insn.set(j++, new ClassInsnNode(Opcodes.NEW, pmap));
                        insn.add(j++, NPInsnNode.of(Opcodes.DUP));
                        insn.add(j++, NPInsnNode.of(Opcodes.ILOAD_2));
                        insn.add(j++, NPInsnNode.of(Opcodes.ILOAD_3));
                        insn.add(j, new InvokeInsnNode(Opcodes.INVOKESPECIAL, pmap, "<init>", "(II)V"));
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("Failed to find [Maps.newHashmap] tag in Chunk");
    }
}
