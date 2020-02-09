package cn.innc11.giftcode.form;

import cn.innc11.giftcode.GiftCodePlugin;
import cn.innc11.giftcode.dt.Codes;
import cn.innc11.giftcode.dt.Gift;
import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class CodesEditPanel extends FormWindowCustom implements FormResponse
{
    String codesUUID;

    public CodesEditPanel(UUID uuid)
    {
        super(TextFormat.colorize("&l设置参数并重新生成"));

        codesUUID = uuid.toString();

        GiftCodePlugin plugin = GiftCodePlugin.ins;
        Codes codes = plugin.getCodesWithUUID(uuid);
        Gift gift = plugin.getGiftWithUUID(codes.giftUuid);
        List<String> gifts = getAllGiftLabels();

        int giftDropDownIndex = (gift == null) ? 0 : gifts.indexOf(gift.label);
        String count = (codes._codeCount == 0) ? "" : String.valueOf(codes._codeCount);
        int minutes = codes.DgetMinutes();
        int hours = codes.DgetHours();
        int days = codes.DgetDays();
        int length = codes._codeLength;

        if (giftDropDownIndex == -1)
        {
            giftDropDownIndex = 0;
        }

        if (length == 0)
        {
            length = 4;
        }

        gifts.add(0, "---");

        addElement(new ElementDropdown("对应的礼包", gifts, giftDropDownIndex));
        addElement(new ElementInput("礼包码数量", "输入0则是公用礼包码", count));
        addElement(new ElementSlider("时限-分钟", 0.0F, 59.0F, 1, minutes));
        addElement(new ElementSlider("时限-小时", 0.0F, 23.0F, 1, hours));
        addElement(new ElementSlider("时限-天", 0.0F, 180.0F, 1, days));
        addElement(new ElementSlider("礼包码的长度", 4.0F, 16.0F, 1, length));
    }

    @Override
    public void onFormClose(PlayerFormRespondedEvent e)
    {
        e.getPlayer().showFormWindow(new CodesPanel(UUID.fromString(codesUUID)));
    }

    public void onFormResponse(PlayerFormRespondedEvent e)
    {
        Player player = e.getPlayer();
        GiftCodePlugin plugin = GiftCodePlugin.ins;

        if (!player.isOp())
            return;

        Codes codes = plugin.getCodesWithUUID(codesUUID);
        String giftLabel = getResponse().getDropdownResponse(0).getElementContent();
        String count = getResponse().getInputResponse(1);
        int minutes = (int) getResponse().getSliderResponse(2);
        int hours = (int) getResponse().getSliderResponse(3);
        int days = (int) getResponse().getSliderResponse(4);
        int length = (int) getResponse().getSliderResponse(5);

        if (!Pattern.matches("^\\d+$", count))
        {
            plugin.sendTitleMessage(player, "礼包码数量只能是数字且不能留空", () -> player.showFormWindow(new CodesEditPanel(codes.uuid)));
            return;
        }

        if (giftLabel.equals("---"))
        {
            plugin.sendTitleMessage(player, "对应的礼包不能为空", () -> player.showFormWindow(new CodesEditPanel(codes.uuid)));
            return;
        }

        int Count = Integer.parseInt(count);
        long timeout = (minutes + hours * 60 + days * 24 * 60);

        codes.giftUuid = plugin.getGiftWithLable(giftLabel).uuid;
        codes._codeCount = Count;
        codes._timeout = timeout;
        codes._codeLength = length;
        codes.isOneTime = (Count != 0);

        codes.regenerate();
        plugin.sendTitleMessage(player, "礼包码已经生成"+ (codes.isOneTime ? (" x &6&l" + codes._codeCount) : ""), () -> player.showFormWindow(new CodesPanel(codes.uuid)));

        plugin.saveGiftCodeSetConfig();
    }

    public ArrayList<String> getAllGiftLabels()
    {
        ArrayList<String> list = new ArrayList<>();
        GiftCodePlugin.ins.gifts.forEach((k, v)->list.add(v.label));
        return list;
    }

}
