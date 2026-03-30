package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Currency;
import models.Exchange;

public class ExchangeCrud implements CrudRepository<Exchange> {

    @Override
    public Exchange findById(Long id) {
        String sqlCommand = """
                            SELECT * FROM public.Exchange
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, id);
            statement.executeQuery();

            var vResultSet = statement.getResultSet();
            var currencyCrud = new CurrencyCrud();
            if (vResultSet.next()) {
                return createNewExchange(vResultSet, currencyCrud);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Exchange> findAll() {
        List<Exchange> entrieslist = new ArrayList<>();
        String sqlCommand = """
                            SELECT * FROM public.Exchange
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.executeQuery();

            var vResultSet = statement.getResultSet();
            var currencyCrud = new CurrencyCrud();
            while (vResultSet.next()) {
                Exchange entry = createNewExchange(vResultSet, currencyCrud);
                entrieslist.add(entry);
            }

            return entrieslist;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long save(Exchange entity) {
        String sqlCommand = """
                            INSERT INTO public.Exchange (baseCurrencyId, targetCurrencyId, rate)
                            VALUES (?, ?, ?)
                            RETURNING id;
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, entity.getBaseCurrency().getId());
            statement.setLong(2, entity.getTargetCurrency().getId());
            statement.setDouble(3, entity.getRate());
            statement.execute();

            var res = statement.getResultSet();
            if (res.next()) {
                return res.getLong(1);
            }
            else {
                return -1L;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Exchange entity) {
        String sqlCommand = """
                            UPDATE public.Exchange
                            SET baseCurrencyId = ?, targetCurrencyId = ?, rate = ?
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, entity.getBaseCurrency().getId());
            statement.setLong(2, entity.getTargetCurrency().getId());
            statement.setDouble(3, entity.getRate());
            statement.setLong(4, entity.getId());
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Long id) {
        String sqlCommand = """
                            DELETE FROM public.Exchange
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, id);
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Exchange findByCodes(String baseCurrencyCode, String targetCurrencyCode) {

        String sqlCommand = """
                            SELECT
                                exchange.id AS id,
                                c1.id AS basecurrencyid,
                                c2.id AS targetcurrencyid,
                                exchange.rate AS rate
                            FROM exchange
                            JOIN currency c1 on c1.id = exchange.basecurrencyid
                            JOIN currency c2 on c2.id = exchange.targetcurrencyid
                            WHERE c1.code = ? and c2.code = ?
                            """;
        List<Exchange> entrieslist = new ArrayList<>();

        try (Connection connection = Utils.ConnectionManager.open();

            PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            statement.executeQuery();

            var vResultSet = statement.getResultSet();
            CurrencyCrud currencyCrud = new CurrencyCrud();
            if (vResultSet.next()) {
                return createNewExchange(vResultSet, currencyCrud);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    private Exchange createNewExchange(ResultSet resultSet, CurrencyCrud currencyCrud) throws SQLException {
        Long baseCurrencyId   = resultSet.getLong("baseCurrencyid");
        Long targetCurrencyId = resultSet.getLong("targetcurrencyid");
        return new Exchange(
                resultSet.getLong("id"),
                currencyCrud.findById(baseCurrencyId),
                currencyCrud.findById(targetCurrencyId),
                resultSet.getDouble("rate"));
    }

}
